package pl.themolka.janusz;

import org.bukkit.Server;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JanuszPlugin extends JavaPlugin {
    private static final char[] PASSWORD = "3HxR7BbhZCZDqJf5".toCharArray();

    private Set<Handler> handlers = Collections.emptySet();

    @Override
    public void onEnable() {
        Server server = this.getServer();
        Logger logger = this.getLogger();

        this.handlers = Stream.of(
                new ChatFormatHandler(),
                new FakePlayerHandler(server, logger),
                new FakePlayerAuthMeHandler(server, logger, PASSWORD),
                new JoinQuitHandler(),
                new PlayerListHandler(),
                new TreeChopHandler()
        ).collect(Collectors.toSet());

        for (Handler handler : this.handlers) {
            try {
                handler.enable(this);
            } catch (Throwable th) {
                logger.log(Level.SEVERE, "Could not enable " + handler.getClass().getSimpleName(), th);
            }
        }
    }

    @Override
    public void onDisable() {
        Logger logger = this.getLogger();
        for (Handler handler : this.handlers) {
            try {
                handler.disable(this);
            } catch (Throwable th) {
                logger.log(Level.SEVERE, "Could not disable " + handler.getClass().getSimpleName(), th);
            }
        }
    }

    public static class Handler implements Listener {
        public void enable(JanuszPlugin plugin) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }

        public void disable(JanuszPlugin plugin) {
            HandlerList.unregisterAll(this);
        }
    }
}
