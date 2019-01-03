package pl.themolka.janusz.arena.event;

import org.bukkit.event.HandlerList;
import pl.themolka.janusz.arena.Match;

public class MatchBeginEvent extends MatchEvent {
    public MatchBeginEvent(Match match) {
        super(match);
    }

    private static final HandlerList HANDLER_LIST = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
