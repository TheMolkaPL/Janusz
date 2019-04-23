package pl.themolka.janusz;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import pl.themolka.janusz.profile.LocalSessionHandler;

import java.util.Objects;

public class JoinQuitHandler extends JanuszPlugin.Handler {
    private static final Message JOIN_MESSAGE = new Message(ChatColor.AQUA + "%s " + ChatColor.DARK_AQUA,
            "dołączył", "dołączyła", "dołączył/a",
            " do serwera");
    private static final Message QUIT_MESSAGE = new Message(
            ChatColor.AQUA + "%s" + ChatColor.DARK_AQUA + " wyszedł z serwera",
            ChatColor.AQUA + "%s" + ChatColor.DARK_AQUA + " wyszła z serwera",
            ChatColor.AQUA + "%s" + ChatColor.DARK_AQUA + " opuścił/a serwer");

    private final JanuszPlugin plugin;

    public JoinQuitHandler(JanuszPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    @EventHandler(priority = EventPriority.LOW)
    public void renameJoinMessage(PlayerJoinEvent event) {
        this.plugin.getHandler(LocalSessionHandler.class).ifPresent(
                handler -> handler.getLocalSession(event.getPlayer()).ifPresent(session -> {
            event.setJoinMessage(String.format(session.format(JOIN_MESSAGE), session.getUsername()));
        }));
    }

    @EventHandler(priority = EventPriority.LOW)
    public void renameQuitMessage(PlayerQuitEvent event) {
        this.plugin.getHandler(LocalSessionHandler.class).ifPresent(
                handler -> handler.getLocalSession(event.getPlayer()).ifPresent(session -> {
            event.setQuitMessage(String.format(session.format(QUIT_MESSAGE), session.getUsername()));
        }));
    }
}
