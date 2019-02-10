package pl.themolka.janusz.clan;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import pl.themolka.janusz.JanuszEvent;
import pl.themolka.janusz.JanuszPlugin;
import pl.themolka.janusz.profile.LocalSession;

import java.util.Objects;
import java.util.Set;

public class ClanChatMessageEvent extends JanuszEvent implements Cancellable {
    private final Clan clan;
    private final LocalSession sender;
    private final String message;
    private final Set<LocalSession> recipients;

    public ClanChatMessageEvent(boolean async, JanuszPlugin plugin, Clan clan, LocalSession sender,
                                String message, Set<LocalSession> recipients) {
        super(async, plugin);

        this.clan = Objects.requireNonNull(clan, "clan");
        this.sender = Objects.requireNonNull(sender, "sender");
        this.message = Objects.requireNonNull(message, "message");
        this.recipients = Objects.requireNonNull(recipients, "recipients");
    }

    public Clan getClan() {
        return this.clan;
    }

    public LocalSession getSender() {
        return this.sender;
    }

    public String getMessage() {
        return this.message;
    }

    public Set<LocalSession> getRecipients() {
        return this.recipients;
    }

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private boolean cancel;

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public boolean isCancelled() {
        return this.cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }
}
