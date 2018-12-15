package pl.themolka.janusz;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinQuitHandler extends JanuszPlugin.Handler {
    private static final Message JOIN_MESSAGE = new Message(ChatColor.AQUA + "%s " + ChatColor.DARK_AQUA,
            "dołączył", "dołączyła", "dołączył/a",
            " do serwera");
    private static final Message QUIT_MESSAGE = new Message(ChatColor.AQUA + "%s " + ChatColor.DARK_AQUA,
            "wyszedł", "wyszła", "opuścił/a",
            " z serwera");

    @EventHandler(priority = EventPriority.LOWEST)
    public void renameJoinMessage(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.setJoinMessage(String.format(JOIN_MESSAGE.format(player), player.getName()));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void renameQuitMessage(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        event.setQuitMessage(String.format(QUIT_MESSAGE.format(player), player.getName()));
    }
}
