package pl.themolka.janusz.season;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import pl.themolka.janusz.Configuration;
import pl.themolka.janusz.JanuszPlugin;
import pl.themolka.janusz.database.Database;
import pl.themolka.janusz.util.PrettyDurationFormatter;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SeasonHandler extends JanuszPlugin.Handler implements SeasonSupplier {
    private final JanuszPlugin plugin;
    private final Configuration configuration;
    private final Database database;

    private final SeasonDao seasonDao;

    private final Map<Long, Season> seasonMap = new ConcurrentHashMap<>();

    private final PrettyDurationFormatter formatter = new PrettyDurationFormatter();

    public SeasonHandler(JanuszPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.configuration = plugin.getConfiguration();
        this.database = plugin.getDb();

        this.seasonDao = this.database.getSeasonDao();
    }

    @Override
    public void enable(JanuszPlugin plugin) {
        super.enable(plugin);
        Logger logger = plugin.getLogger();

        List<Season> seasons = Collections.emptyList();
        try {
            logger.info("Getting seasons from the database...");
            seasons = this.database.getExecutor().submit(this.seasonDao::findAll).get(10L, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.log(Level.SEVERE, "Could not get seasons", e);
            this.plugin.getServer().shutdown(); // for security reasons
        }

        seasons.forEach(season -> this.seasonMap.put(season.getId(), season));

        try {
            Season current = this.current();
            logger.info("Current season: " + current.getId() + " (from " + current.getFrom().toString() +  ")");
        } catch (IllegalArgumentException e) {
            logger.log(Level.SEVERE, "Could not resolve current season", e);
            plugin.getServer().shutdown();
        }
    }

    public Optional<Season> findSeason(long id) {
        return Optional.ofNullable(this.seasonMap.get(id));
    }

    @Override
    public Season apply(Long id) {
        return this.findSeason(id).orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public Season current() {
        return this.apply(this.configuration.getCurrentSeasonId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLoginBeforeOrAfterSeason(AsyncPlayerPreLoginEvent event) {
        LocalDateTime now = LocalDateTime.now();
        Season season = this.current();

        LocalDateTime from = season.getFrom();
        if (from != null && from.isAfter(now)) {
            Duration duration = Duration.between(Instant.now(), from.toInstant(ZoneOffset.ofHours(1)));

            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, ChatColor.RED + "Sezon zaczyna się za " +
                    ChatColor.GREEN + this.formatter.format(duration, ChatColor.RED + "i" + ChatColor.GREEN) +
                    ChatColor.RED + ".");
            return;
        }

        LocalDateTime to = season.getTo();
        if (to != null && to.isBefore(now)) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, ChatColor.RED + "Sezon się zakończył.");
        }
    }
}
