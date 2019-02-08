package pl.themolka.janusz.clan;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.ScoreboardManager;
import pl.themolka.janusz.JanuszPlugin;
import pl.themolka.janusz.database.Database;
import pl.themolka.janusz.profile.LocalSession;
import pl.themolka.janusz.profile.LocalSessionHandler;
import pl.themolka.janusz.profile.Profile;
import pl.themolka.janusz.season.SeasonSupplier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

public class ClanHandler extends JanuszPlugin.Handler {
    private final JanuszPlugin plugin;
    private final Database database;

    private final ClanDao clanDao;

    private final Map<Long, Clan> byId = new ConcurrentHashMap<>();
    private final Map<String, Clan> byTeam = new ConcurrentHashMap<>();

    public ClanHandler(JanuszPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.database = plugin.getDb();

        this.clanDao = this.database.getClanDao();
    }

    @Override
    public void enable(JanuszPlugin plugin) {
        super.enable(plugin);

        SeasonSupplier seasons = plugin.getSeasons();
        try {
            this.database.getExecutor()
                    .submit(() -> this.clanDao.findAll(seasons))
                    .get(10, TimeUnit.SECONDS)
                    .forEach(this::addClan);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not fetch clans", e);
        }

        ScoreboardManager scoreboardManager = plugin.getServer().getScoreboardManager();
        this.getClans().forEach(clan -> clan.applyValues(scoreboardManager));
    }

    public void addClan(Clan clan) {
        Objects.requireNonNull(clan, "clan");
        this.byId.put(clan.getId(), clan);
        this.byTeam.put(clan.getTeam(), clan);
    }

    public void flushClans() {
        this.byId.clear();
        this.byTeam.clear();
    }

    public Optional<Clan> get(long id) {
        return Optional.ofNullable(this.byId.get(id));
    }

    public Optional<Clan> get(String team) {
        return Optional.ofNullable(this.byTeam.get(Objects.requireNonNull(team, "team")));
    }

    public List<Clan> getClans() {
        return new ArrayList<>(this.byId.values());
    }

    public Optional<Clan> getFor(Profile profile) {
        return this.getFor(Objects.requireNonNull(profile, "profile").getId());
    }

    public Optional<Clan> getFor(long profileId) {
        return this.byId.values().stream()
                .filter(clan -> clan.contains(profileId))
                .findFirst();
    }

    public void removeClan(Clan clan) {
        Objects.requireNonNull(clan, "clan");
        this.byId.remove(clan.getId());
        this.byTeam.remove(clan.getTeam());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent event) {
        LocalSessionHandler localSessionHandler = this.plugin.getHandler(LocalSessionHandler.class).orElse(null);
        if (localSessionHandler == null) {
            return;
        }

        Player player = event.getPlayer();

        LocalSession localSession = localSessionHandler.getLocalSession(player).orElse(null);
        if (localSession == null) {
            return;
        }

        this.getFor(localSession.getProfile()).ifPresent(clan -> {
            ScoreboardManager scoreboardManager = this.plugin.getServer().getScoreboardManager();
            clan.getBukkit(scoreboardManager).addEntry(player.getName());

            player.setScoreboard(scoreboardManager.getMainScoreboard());
        });
    }
}
