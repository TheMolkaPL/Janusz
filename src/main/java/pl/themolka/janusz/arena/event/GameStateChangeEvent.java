package pl.themolka.janusz.arena.event;

import org.bukkit.event.HandlerList;
import pl.themolka.janusz.JanuszPlugin;
import pl.themolka.janusz.arena.GameState;

import java.util.Objects;
import java.util.Optional;

public class GameStateChangeEvent extends GameEvent {
    private final GameState oldState;
    private final GameState newState;

    public GameStateChangeEvent(JanuszPlugin plugin, GameState oldState, GameState newState) {
        super(plugin, newState.getGame());

        this.oldState = oldState;
        this.newState = Objects.requireNonNull(newState, "newState");
    }

    public Optional<GameState> getOldState() {
        return Optional.ofNullable(this.oldState);
    }

    public GameState getNewState() {
        return this.newState;
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
