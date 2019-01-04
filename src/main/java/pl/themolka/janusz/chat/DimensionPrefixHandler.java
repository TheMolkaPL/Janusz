package pl.themolka.janusz.chat;

import com.google.common.collect.ImmutableMap;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import pl.themolka.janusz.JanuszPlugin;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class DimensionPrefixHandler extends JanuszPlugin.Handler {
    private static final Map<World.Environment, Prefix> PREFIXES = ImmutableMap.<World.Environment, Prefix>builder()
            .put(World.Environment.NORMAL, new OverworldPrefix())
            .put(World.Environment.NETHER, new Prefix(ChatColor.RED, "Nether"))
            .put(World.Environment.THE_END, new Prefix(ChatColor.YELLOW, "The End"))
            .build();

    @EventHandler(priority = EventPriority.HIGH)
    public void applyPlayerPrefix(AsyncPlayerChatEvent event) {
        Optional.ofNullable(PREFIXES.get(event.getPlayer().getWorld().getEnvironment()))
                .ifPresent(prefix -> event.setFormat(prefix.format(event.getFormat())));
    }

    static class Prefix {
        final ChatColor color;
        final String title;

        Prefix(ChatColor color, String title) {
            this.color = Objects.requireNonNull(color, "color");
            this.title = Objects.requireNonNull(title, "title");
        }

        String format(String format) {
            return this.color + "[" + ChatColor.ITALIC + this.title + ChatColor.RESET + this.color + "] " + format;
        }
    }

    static class OverworldPrefix extends Prefix {
        OverworldPrefix() {
            super(ChatColor.WHITE, "Overworld");
        }

        @Override
        String format(String format) {
            return format;
        }
    }
}
