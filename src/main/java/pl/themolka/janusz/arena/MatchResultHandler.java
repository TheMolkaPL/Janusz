package pl.themolka.janusz.arena;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import pl.themolka.janusz.JanuszPlugin;
import pl.themolka.janusz.arena.event.MatchEndEvent;
import pl.themolka.janusz.database.Database;

import java.util.Objects;

public class MatchResultHandler extends JanuszPlugin.Handler {
    private final JanuszPlugin plugin;
    private final Database database;

    private final MatchResultDao matchResultDao;

    public MatchResultHandler(JanuszPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.database = plugin.getDb();

        this.matchResultDao = this.database.getMatchResultDao();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void logMatchResultInDatabase(MatchEndEvent event) {
        MatchResult result = event.getResult();
        if (result.getWinner().isPresent()) {
            this.database.getExecutor().submit(() -> this.matchResultDao.save(result));
        }
    }
}
