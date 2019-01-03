package pl.themolka.janusz;

import org.bukkit.Server;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FakePlayerAuthMeHandler extends JanuszPlugin.Handler {
    private final JanuszPlugin plugin;
    private final Configuration.FakePlayer configuration;

    private final Server server;
    private final Logger logger;

    public FakePlayerAuthMeHandler(JanuszPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.configuration = plugin.getConfiguration().getFakePlayer();

        this.server = plugin.getServer();
        this.logger = plugin.getLogger();
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void autoLogin(FakePlayerHandler.FakePlayerRegisterEvent event) {
        if (this.plugin == null) {
            return;
        }

        String name = this.configuration.getAuthMeLoginCommand();

        this.server.getScheduler().runTaskLater(this.plugin, () -> {
            FakePlayerHandler handler = event.getHandler();
            FakePlayerHandler.FakePlayer fakePlayer = event.getSession().getPlayer();

            if (handler.resolvePlayerList(this.server).players.contains(fakePlayer)) {
                PluginCommand command = this.server.getPluginCommand(name);

                if (command != null) {
                    this.logger.log(Level.INFO, "Logging in the fake player...");
                    command.execute(fakePlayer.getBukkitEntity(),
                                    name,
                                    new String[] {String.valueOf(this.configuration.getAuthMePassword())});
                }
            }
        }, 20L * 3);
    }
}
