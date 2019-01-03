package pl.themolka.janusz.arena.event;

import org.bukkit.event.HandlerList;
import pl.themolka.janusz.arena.Match;
import pl.themolka.janusz.arena.MatchResult;

import java.util.Objects;

public class MatchEndEvent extends MatchEvent {
    private final MatchResult result;

    public MatchEndEvent(Match match, MatchResult result) {
        super(match);
        this.result = Objects.requireNonNull(result, "result");
    }

    public MatchResult getResult() {
        return this.result;
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
