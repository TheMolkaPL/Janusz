package pl.themolka.janusz.arena;

import pl.themolka.janusz.profile.LocalSession;

import java.util.Objects;
import java.util.Set;

public class GameStateFactory {
    private final Game game;

    public GameStateFactory(Game game) {
        this.game = Objects.requireNonNull(game, "game");
    }

    public Idle idle() {
        return new Idle(this.game);
    }

    public Idle idle(Set<LocalSession> queue) {
        return new Idle(this.game, queue);
    }

    public Starting starting(Set<LocalSession> queue) {
        return new Starting(this.game, queue, this.game.getArena().getSpawns());
    }

    public Match match(Set<LocalSession> competitors) {
        return new Match(this.game, competitors);
    }
}
