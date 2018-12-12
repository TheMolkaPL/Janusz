package pl.themolka.janusz;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatFormatHandler extends JanuszPlugin.Handler {
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        event.setFormat(ChatColor.AQUA + "%s" + ChatColor.DARK_AQUA + ": " + ChatColor.GRAY + "%s");
    }
}
