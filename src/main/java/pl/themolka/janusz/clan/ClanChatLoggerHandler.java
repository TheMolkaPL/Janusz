package pl.themolka.janusz.clan;

import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import pl.themolka.janusz.JanuszPlugin;
import pl.themolka.janusz.database.Database;
import pl.themolka.janusz.profile.LocalSession;
import pl.themolka.janusz.profile.LocalSessionHandler;
import pl.themolka.janusz.season.Season;

import java.time.LocalDateTime;
import java.util.Objects;

public class ClanChatLoggerHandler extends JanuszPlugin.Handler {
    private final JanuszPlugin plugin;
    private final Database database;

    private final ClanChatDao clanChatDao;

    public ClanChatLoggerHandler(JanuszPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.database = plugin.getDb();

        this.clanChatDao = this.database.getClanChatDao();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void logClanChatInDatabase(ClanChatMessageEvent event) {
        String text = event.getMessage();
        if (StringUtils.isEmpty(text)) {
            // This shouldn't happen anyway.
            return;
        }

        LocalSession sender = event.getSender();

        Player bukkit = sender.getBukkit().orElse(null);
        if (bukkit == null) {
            return;
        }

        String world = bukkit.getWorld().getName();

        ClanHandler clanHandler = this.plugin.getHandler(ClanHandler.class).orElse(null);
        if (clanHandler == null) {
            return;
        }

        LocalSessionHandler localSessionHandler = this.plugin.getHandler(LocalSessionHandler.class).orElse(null);
        if (localSessionHandler == null) {
            return;
        }

        Season season = this.plugin.getSeasons().current();

        boolean sent = !event.isCancelled();
        int recipientCount = (int) event.getRecipients().stream()
                .filter(player -> !player.getUniqueId().equals(sender.getUniqueId()))
                .count();

        ClanChat chat = new ClanChat(LocalDateTime.now(), season, event.getClan(), sender, world, text, sent, recipientCount);
        this.database.getExecutor().submit(() -> this.clanChatDao.save(chat));
    }
}
