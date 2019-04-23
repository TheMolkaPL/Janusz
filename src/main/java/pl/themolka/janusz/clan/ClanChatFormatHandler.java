package pl.themolka.janusz.clan;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import pl.themolka.janusz.JanuszPlugin;
import pl.themolka.janusz.profile.LocalSession;
import pl.themolka.janusz.profile.LocalSessionHandler;

public class ClanChatFormatHandler extends JanuszPlugin.Handler {
    private final JanuszPlugin plugin;

    public ClanChatFormatHandler(JanuszPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void format(AsyncPlayerChatEvent event) {
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

        Clan clan = clanHandler.getFor(localSession.getProfile()).orElse(null);
        if (clan == null) {
            return;
        }

        event.setFormat(clan.getColor().toString() + "[" + clan.getTitle() + "] " + event.getFormat());
        // ^ aren't clan titles too long for this? :/
    }
}
