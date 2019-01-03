package pl.themolka.janusz.arena.event;

import pl.themolka.janusz.JanuszPlugin;
import pl.themolka.janusz.arena.Game;

import java.util.Objects;

public abstract class GameEvent extends ArenaEvent {
    private final Game game;

    public GameEvent(JanuszPlugin plugin, Game game) {
        super(plugin, Objects.requireNonNull(game, "game").getArena());
        this.game = game;
    }

    public GameEvent(boolean isAsync, JanuszPlugin plugin, Game game) {
        super(isAsync, plugin, Objects.requireNonNull(game, "game").getArena());
        this.game = game;
    }

    public Game getGame() {
        return this.game;
    }
}
