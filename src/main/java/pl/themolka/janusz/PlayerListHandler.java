package pl.themolka.janusz;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerListHandler extends JanuszPlugin.Handler {
    @EventHandler(priority = EventPriority.NORMAL)
    public void applyName(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String name = player.getName();

        String color = ChatColor.GRAY.toString();
        player.setPlayerListName(color + name.substring(0, Math.min(name.length(), 16 - color.length())));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void applyFooter(PlayerJoinEvent event) {
        this.updateFooter(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void applyFooter(PlayerTeleportEvent event) {
        this.updateFooter(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void applyFooter(PlayerPortalEvent event) {
        this.updateFooter(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void applyFooter(PlayerChangedWorldEvent event) {
        this.updateFooter(event.getPlayer());
    }

    private void updateFooter(Player player) {
        Location spawn = player.getWorld().getSpawnLocation();
        player.setPlayerListFooter("   " + ChatColor.GREEN + "Spawn znajduje się na " +
                this.getCoordinate('X', spawn.getBlockX()) + ChatColor.GREEN + ", " +
                this.getCoordinate('Z', spawn.getBlockZ()) + "   ");
    }

    private String getCoordinate(char axis, int at) {
        return ChatColor.GOLD.toString() + Character.toUpperCase(axis) + ": " + ChatColor.BOLD + at + ChatColor.RESET;
    }
}
