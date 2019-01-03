package pl.themolka.janusz.arena;

import org.apache.commons.lang3.Validate;
import pl.themolka.janusz.JanuszPlugin;
import pl.themolka.janusz.arena.event.GameStateChangeEvent;
import pl.themolka.janusz.profile.LocalSession;

import java.util.Objects;
import java.util.logging.Logger;

public class Game {
    protected final JanuszPlugin plugin;
    protected final Logger logger;

    private final Arena arena;
    private final int minPlayerCount;
    private final GameStateFactory factory;
    private GameState state;

    public Game(JanuszPlugin plugin, Logger logger, Arena arena, int minPlayerCount) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.logger = Objects.requireNonNull(logger, "logger");

        this.arena = Objects.requireNonNull(arena, "arena");
        this.minPlayerCount = minPlayerCount;
        this.factory = new GameStateFactory(this);

        Validate.isTrue(minPlayerCount <= arena.getSpawns().size(), "minPlayerCount larger than number of spawns!");

        this.state = this.factory.idle();
    }

    public Logger getLogger() {
        return this.logger;
    }

    public Arena getArena() {
        return this.arena;
    }

    public int getMinPlayerCount() {
        return this.minPlayerCount;
    }

    public GameStateFactory getFactory() {
        return this.factory;
    }

    public GameState getState() {
        return this.state;
    }

    public <T extends GameState> T transform(T newState) {
        Objects.requireNonNull(newState, "newState");

        this.logger.info("Transforming from '" + this.state + "' to '" + newState + "'.");

        if (this.state != null) {
            this.state.disableState();
        }

        GameState oldState = this.state;
        this.state = newState;

        this.plugin.callEvent(new GameStateChangeEvent(this.plugin, oldState, this.state));

        newState.enableState();

        return newState;
    }

    public boolean canJoin(LocalSession competitor) {
        return this.state.canJoin(Objects.requireNonNull(competitor, "competitor"));
    }

    public boolean join(LocalSession competitor) {
        return this.state.join(Objects.requireNonNull(competitor, "competitor"));
    }

    public boolean leave(LocalSession competitor) {
        return this.state.leave(Objects.requireNonNull(competitor, "competitor"));
    }
}
