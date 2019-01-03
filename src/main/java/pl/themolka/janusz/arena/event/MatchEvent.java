package pl.themolka.janusz.arena.event;

import pl.themolka.janusz.arena.Match;

import java.util.Objects;

public abstract class MatchEvent extends GameEvent {
    private final Match match;

    public MatchEvent(Match match) {
        super(Objects.requireNonNull(match, "match").getPlugin(), match.getGame());
        this.match = match;
    }

    public MatchEvent(boolean isAsync, Match match) {
        super(isAsync, Objects.requireNonNull(match, "match").getPlugin(), match.getGame());
        this.match = match;
    }

    public Match getMatch() {
        return this.match;
    }
}
