package pl.themolka.janusz.chat;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import pl.themolka.janusz.JanuszPlugin;
import pl.themolka.janusz.clan.ClanChatHandler;
import pl.themolka.janusz.database.Database;
import pl.themolka.janusz.profile.LocalSessionHandler;
import pl.themolka.janusz.season.Season;

import java.time.LocalDateTime;
import java.util.Objects;

public class ChatLoggerHandler extends JanuszPlugin.Handler {
    private final JanuszPlugin plugin;
    private final Database database;

    private final ChatDao chatDao;

    public ChatLoggerHandler(JanuszPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.database = plugin.getDb();

        this.chatDao = this.database.getChatDao();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void logPlayerChatInDatabase(AsyncPlayerChatEvent event) {
        String text = event.getMessage();
        if (StringUtils.isEmpty(text)) {
            // This shouldn't happen anyway.
            return;
        }

        if (text.startsWith(ClanChatHandler.CHANNEL_KEY)) {
            // This is ugly :(
            return;
        }

        Player bukkit = event.getPlayer();
        String world = bukkit.getWorld().getName();

        this.plugin.getHandler(LocalSessionHandler.class).ifPresent(
                handler -> handler.getLocalSession(bukkit).ifPresent(session -> {
            Season season = this.plugin.getSeasons().current();

            boolean sent = !event.isCancelled();
            int recipientCount = (int) event.getRecipients().stream()
                    .filter(player -> !player.getUniqueId().equals(session.getUniqueId()))
                    .count();

            Chat chat = new Chat(LocalDateTime.now(), season, session, world, text, sent, recipientCount);
            this.database.getExecutor().submit(() -> this.chatDao.save(chat));
        }));
    }
}
