package pl.themolka.janusz;

import org.bukkit.Server;
import org.bukkit.event.Event;

import java.util.Objects;

public abstract class JanuszEvent extends Event {
    protected JanuszPlugin plugin;

    public JanuszEvent(JanuszPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    public JanuszEvent(boolean isAsync, JanuszPlugin plugin) {
        super(isAsync);
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    public JanuszPlugin getPlugin() {
        return this.plugin;
    }

    public Server getServer() {
        return this.plugin.getServer();
    }
}
