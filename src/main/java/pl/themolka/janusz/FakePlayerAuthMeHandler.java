package pl.themolka.janusz;

import org.bukkit.Server;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FakePlayerAuthMeHandler extends JanuszPlugin.Handler {
    private final Server server;
    private final Logger logger;
    private final char[] password;

    private JanuszPlugin plugin;

    public FakePlayerAuthMeHandler(Server server, Logger logger, char[] password) {
        this.server = Objects.requireNonNull(server, "server");
        this.logger = Objects.requireNonNull(logger, "logger");
        this.password = Objects.requireNonNull(password, "password");
    }

    @Override
    public void enable(JanuszPlugin plugin) {
        super.enable(plugin);
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void autoLogin(FakePlayerHandler.FakePlayerRegisterEvent event) {
        if (this.plugin == null) {
            return;
        }

        String name = "login";

        this.server.getScheduler().runTaskLater(this.plugin, () -> {
            FakePlayerHandler handler = event.getHandler();
            FakePlayerHandler.FakePlayer fakePlayer = event.getSession().getPlayer();

            if (handler.resolvePlayerList(this.server).players.contains(fakePlayer)) {
                PluginCommand command = this.server.getPluginCommand(name);

                if (command != null) {
                    this.logger.log(Level.INFO, "Logging in the fake player...");
                    command.execute(fakePlayer.getBukkitEntity(),
                                    name,
                                    new String[] {String.valueOf(this.password)});
                }
            }
        }, 20L * 3);
    }
}
