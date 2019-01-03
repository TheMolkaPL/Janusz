package pl.themolka.janusz.profile;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import pl.themolka.janusz.JanuszPlugin;
import pl.themolka.janusz.database.Database;
import pl.themolka.janusz.season.Season;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

public class LocalSessionHandler extends JanuszPlugin.Handler {
    private final JanuszPlugin plugin;
    private final Database database;

    private final Cache<UUID, Login> queue = CacheBuilder.newBuilder()
            .expireAfterWrite(10L, TimeUnit.SECONDS)
            .build();

    private final Map<UUID, LocalSession> byId = new ConcurrentHashMap<>();
    private final Map<String, LocalSession> byName = new ConcurrentHashMap<>();

    private final SessionDao sessionDao;

    public LocalSessionHandler(JanuszPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.database = this.plugin.getDb();

        this.sessionDao = this.database.getSessionDao();
    }

    @Override
    public void enable(JanuszPlugin plugin) {
        this.cleanUp();
        super.enable(plugin);
    }

    @Override
    public void disable(JanuszPlugin plugin) {
        super.disable(plugin);
        this.cleanUp();
    }

    private void cleanUp() {
        try {
            this.database.getExecutor().submit(() -> this.sessionDao.destroyAll(LocalDateTime.now())).get(10L, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not clean up player sessions", e);
        }
    }

    public void addLocalSession(LocalSession localSession) {
        Objects.requireNonNull(localSession, "localSession");
        this.byId.put(localSession.getUniqueId(), localSession);
        this.byName.put(Session.normalizeUsername(localSession.getUsername()), localSession);
    }

    public Optional<LocalSession> findLocalSession(String query) {
        query = Objects.requireNonNull(query, "query");

        if (query.length() == 36) {
            try {
                return this.getLocalSession(UUID.fromString(query));
            } catch (IllegalArgumentException ignored) {
            }
        }

        String lowerQuery = Session.normalizeUsername(query);

        Optional<LocalSession> result = this.getLocalSession(lowerQuery);
        if (result.isPresent()) {
            return result;
        }

        return this.byId.values().stream()
                .filter(profile -> Session.normalizeUsername(profile.getUsername()).contains(lowerQuery))
                .findFirst();
    }

    public Optional<LocalSession> getLocalSession(UUID uniqueId) {
        return Optional.ofNullable(this.byId.get(Objects.requireNonNull(uniqueId, "uniqueId")));
    }

    public Optional<LocalSession> getLocalSession(String username) {
        return Optional.ofNullable(this.byName.get(Session.normalizeUsername(Objects.requireNonNull(username, "username"))));
    }

    public Optional<LocalSession> getLocalSession(CommandSender sender) {
        return sender instanceof Player ? this.getLocalSession((Entity) sender) : Optional.empty();
    }

    public Optional<LocalSession> getLocalSession(Entity entity) {
        return this.getLocalSession(Objects.requireNonNull(entity, "entity").getUniqueId());
    }

    public void removeLocalSession(LocalSession localSession) {
        Objects.requireNonNull(localSession, "localSession");
        this.byId.remove(localSession.getUniqueId());
        this.byName.remove(Session.normalizeUsername(localSession.getUsername()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        UUID uniqueId = event.getUniqueId();
        this.plugin.getHandler(ProfileHandler.class).ifPresent(handler -> handler.getProfile(uniqueId).ifPresent(profile -> {
            this.queue.put(uniqueId, new Login(profile));
        }));
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        UUID uniqueId = player.getUniqueId();

        Season season = this.plugin.getSeasons().current();

        this.plugin.getLogger().info("'" + uniqueId.toString() + "' is known as " + player.getName());

        Optional.ofNullable(this.queue.getIfPresent(uniqueId)).ifPresent(login -> {
            LocalSession localSession = new LocalSession(this.plugin, LocalDateTime.now(), season, login.profile, player);
            this.addLocalSession(localSession);

            try {
                this.plugin.getLogger().info("Creating new session for " + localSession.getUsername() + "...");
                this.database.getExecutor().submit(() -> this.sessionDao.save(localSession)).get(10L, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                this.plugin.getLogger().log(Level.SEVERE, "Could not handle player local session", e);
            } finally {
                this.queue.invalidate(uniqueId);
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.getLocalSession(event.getPlayer()).ifPresent(localSession -> this.database.getExecutor().submit(() -> {
            this.removeLocalSession(localSession);

            this.plugin.getLogger().info("Destroying sessions for " + localSession.getUsername() + "...");
            this.sessionDao.destroyAllForProfile(localSession.getProfile().getId(), LocalDateTime.now());
        }));
    }

    class Login {
        final Profile profile;

        Login(Profile profile) {
            this.profile = Objects.requireNonNull(profile, "profile");
        }
    }
}
