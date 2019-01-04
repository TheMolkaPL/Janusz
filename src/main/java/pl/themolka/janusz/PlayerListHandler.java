package pl.themolka.janusz;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Objects;

public class PlayerListHandler extends JanuszPlugin.Handler {
    private static final int MAX_NAME_LENGTH = 16;

    @EventHandler(priority = EventPriority.NORMAL)
    public void apply(PlayerJoinEvent event) {
        this.update(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void apply(PlayerTeleportEvent event) {
        this.update(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void apply(PlayerPortalEvent event) {
        this.update(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void apply(PlayerChangedWorldEvent event) {
        this.update(event.getPlayer());
    }

    private void update(Player player) {
        Objects.requireNonNull(player, "player");
        this.updateName(player);
        this.updateFooter(player);
    }

    private void updateName(Player player) {
        String color = this.getColor(player.getWorld()).toString();
        String name = player.getName();
        player.setPlayerListName(color + name.substring(0, Math.min(name.length(), MAX_NAME_LENGTH - color.length())));
    }

    private ChatColor getColor(World world) {
        switch (world.getEnvironment()) {
            case NORMAL:
                return ChatColor.GREEN;
            case NETHER:
                return ChatColor.RED;
            case THE_END:
                return ChatColor.YELLOW;
            default:
                return ChatColor.GRAY;
        }
    }

    private void updateFooter(Player player) {
        Location spawn = player.getWorld().getSpawnLocation();
        ChatColor color = this.getColor(spawn.getWorld());
        player.setPlayerListFooter("   " + color + "Spawn znajduje się na " +
                this.getCoordinate('X', spawn.getBlockX()) + color + ", " +
                this.getCoordinate('Z', spawn.getBlockZ()) + "   ");
    }

    private String getCoordinate(char axis, int at) {
        return ChatColor.GOLD.toString() + Character.toUpperCase(axis) + ": " + ChatColor.BOLD + at + ChatColor.RESET;
    }
}
