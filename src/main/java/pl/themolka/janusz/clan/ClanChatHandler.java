package pl.themolka.janusz.clan;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import pl.themolka.janusz.JanuszPlugin;
import pl.themolka.janusz.profile.LocalSession;
import pl.themolka.janusz.profile.LocalSessionHandler;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ClanChatHandler extends JanuszPlugin.Handler {
    public static final String CHANNEL_KEY = "@";

    private final JanuszPlugin plugin;

    public ClanChatHandler(JanuszPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void clanChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        if (!message.startsWith(CHANNEL_KEY)) {
            return;
        }

        ClanHandler clanHandler = this.plugin.getHandler(ClanHandler.class).orElse(null);
        if (clanHandler == null) {
            return;
        }

        LocalSessionHandler localSessionHandler = this.plugin.getHandler(LocalSessionHandler.class).orElse(null);
        if (localSessionHandler == null) {
            return;
        }

        LocalSession localSession = localSessionHandler.getLocalSession(event.getPlayer()).orElse(null);
        if (localSession == null) {
            return;
        }

        event.setCancelled(true);

        Clan clan = clanHandler.getFor(localSession.getProfile()).orElse(null);
        if (clan == null) {
            localSession.printError("Nie jesteś w żadnym klanie :(");
            return;
        }

        message = message.substring(1);
        if (StringUtils.isEmpty(message)) {
            localSession.printError("Nie podano treści wiadomości");
            return;
        }

        Set<LocalSession> recipients = this.plugin.getServer().getOnlinePlayers().stream()
                .map(localSessionHandler::getLocalSession)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        ClanChatMessageEvent messageEvent = new ClanChatMessageEvent(
                event.isAsynchronous(), clan, localSession, message, recipients);
        this.plugin.callEvent(messageEvent);

        if (messageEvent.isCancelled()) {
            return;
        }

        String username = localSession.getUsername();
        String finalMessage = messageEvent.getMessage();

        this.plugin.getLogger().log(Level.INFO, "[Clan-Chat] [" + messageEvent.getClan().getId() +
                "] " + username + ": " + finalMessage);

        String recipientMessage = clan.getColor() + ChatColor.ITALIC.toString() + "[KLAN] " +
                ChatColor.RESET + clan.getColor() + username + ": " + ChatColor.GRAY + finalMessage;
        for (LocalSession recipient : messageEvent.getRecipients()) {
            recipient.print(recipientMessage);
        }
    }
}
