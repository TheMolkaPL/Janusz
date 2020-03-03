package pl.themolka.janusz.motd;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.server.ServerListPingEvent;
import pl.themolka.janusz.JanuszPlugin;
import pl.themolka.janusz.database.Database;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

public class MotdHandler extends JanuszPlugin.Handler {
    private static final Duration CACHE_EXPIRATION = Duration.ofMinutes(1);

    private final JanuszPlugin plugin;
    private final Database database;

    private final MotdDao motdDao;
    private final Random random = new Random();

    private CachedMotds cachedMotds;

    public MotdHandler(JanuszPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.database = plugin.getDb();

        this.motdDao = this.database.getMotdDao();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerListPing(ServerListPingEvent event) {
        // get cached first
        List<Motd> motds = this.getCached();

        if (motds == null) {
            // find all valid from the database and cache the results
            motds = this.findAllValid();
            this.cachedMotds = new CachedMotds(Instant.now(), motds);
        }

        if (motds != null && !motds.isEmpty()) {
            event.setMotd(motds.get(this.random.nextInt(motds.size())).getText());
        }
    }

    private List<Motd> getCached() {
        if (this.cachedMotds == null) {
            return null;
        }

        Instant expiresAt = this.cachedMotds.cachedAt.plus(CACHE_EXPIRATION);
        return Instant.now().isBefore(expiresAt) ? this.cachedMotds : null;
    }

    private List<Motd> findAllValid() {
        try {
            return this.database.getExecutor().submit(this.motdDao::findAllValid).get(1L, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not get motds", e);
            return null;
        }
    }

    private class CachedMotds extends ArrayList<Motd> {
        Instant cachedAt;

        CachedMotds(Instant cachedAt, List<Motd> list) {
            this.cachedAt = cachedAt;
            if (list != null) {
                this.addAll(list);
            }
        }
    }
}
