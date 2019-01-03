package pl.themolka.janusz.motd;

import org.apache.commons.lang3.RandomUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.server.ServerListPingEvent;
import pl.themolka.janusz.JanuszPlugin;
import pl.themolka.janusz.database.Database;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

public class MotdHandler extends JanuszPlugin.Handler {
    private final JanuszPlugin plugin;
    private final Database database;

    private final MotdDao motdDao;

    private final Random random = new Random();

    public MotdHandler(JanuszPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.database = plugin.getDb();

        this.motdDao = this.database.getMotdDao();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerListPing(ServerListPingEvent event) {
        List<Motd> motds = Collections.emptyList();
        try {
            motds = this.database.getExecutor().submit(this.motdDao::findAllValid).get(10L, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not get motds", e);
        }

        if (!motds.isEmpty()) {
            event.setMotd(motds.get(this.random.nextInt(motds.size())).getText());
        }
    }
}
