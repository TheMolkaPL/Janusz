package pl.themolka.janusz.season;

import pl.themolka.janusz.Configuration;
import pl.themolka.janusz.JanuszPlugin;
import pl.themolka.janusz.database.Database;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

public class SeasonHandler extends JanuszPlugin.Handler implements SeasonSupplier {
    private final JanuszPlugin plugin;
    private final Configuration configuration;
    private final Database database;

    private final SeasonDao seasonDao;

    private final Map<Long, Season> seasonMap = new HashMap<>();

    public SeasonHandler(JanuszPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.configuration = plugin.getConfiguration();
        this.database = plugin.getDb();

        this.seasonDao = this.database.getSeasonDao();
    }

    @Override
    public void enable(JanuszPlugin plugin) {
        super.enable(plugin);

        List<Season> seasons = Collections.emptyList();
        try {
            this.plugin.getLogger().info("Getting seasons from the database...");
            seasons = this.database.getExecutor().submit(this.seasonDao::findAll).get(10L, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not get seasons", e);
            this.plugin.getServer().shutdown(); // for security reasons
        }

        seasons.forEach(season -> this.seasonMap.put(season.getId(), season));

        try {
            Season current = this.current();
            this.plugin.getLogger().info("Current season: " + current.getId() + " (from " + current.getFrom().toString() +  ")");
        } catch (IllegalArgumentException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not resolve current season", e);
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
}
