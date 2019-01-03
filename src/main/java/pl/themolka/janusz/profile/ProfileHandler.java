package pl.themolka.janusz.profile;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import pl.themolka.janusz.JanuszPlugin;
import pl.themolka.janusz.database.Database;

import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class ProfileHandler extends JanuszPlugin.Handler {
    private final JanuszPlugin plugin;
    private final Database database;

    private final Cache<UUID, Profile> profiles = CacheBuilder.newBuilder()
            .expireAfterWrite(10L, TimeUnit.SECONDS)
            .build();

    private final ProfileDao profileDao;

    public ProfileHandler(JanuszPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.database = plugin.getDb();

        this.profileDao = this.database.getProfileDao();
    }

    public Optional<Profile> getProfile(UUID uniqueId) {
        return Optional.ofNullable(this.profiles.getIfPresent(Objects.requireNonNull(uniqueId)));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        UUID uniqueId = event.getUniqueId();

        try {
            Profile profile = this.database.getExecutor().submit(() -> this.profileDao.find(uniqueId).orElseGet(() -> {
                try {
                    this.plugin.getLogger().info("Havn't seen profile '" + uniqueId + "' yet. Registering him for the first time...");
                    Profile save = new Profile(uniqueId);
                    this.profileDao.save(save);
                    return save;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            })).get(10L, TimeUnit.SECONDS);

            this.plugin.getLogger().info("Logging in '" + profile.getUniqueId() + "' (" + profile.getId() + ")...");
            this.profiles.put(uniqueId, profile);
        } catch (Exception e) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not handle player profile", e);
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ChatColor.RED + "Wystąpił problem z ustaleniem twojego profilu gry.");
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (this.profiles.getIfPresent(event.getPlayer().getUniqueId()) == null) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, ChatColor.RED + "Wystąpił problem z ustaleniem twojego profilu gry");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.profiles.invalidate(event.getPlayer().getUniqueId());
    }
}
