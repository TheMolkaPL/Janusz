package pl.themolka.janusz.arena;

import pl.themolka.janusz.profile.LocalSession;

import java.util.Set;

public class Idle extends GameState.Queue {
    public Idle(Game game) {
        this(game, null);
    }

    public Idle(Game game, Set<LocalSession> queue) {
        super(game, queue);
    }

    @Override
    public void enableState() {
        Spawn defaultSpawn = this.game.getArena().getDefaultSpawn();
        this.queue.forEach(defaultSpawn::spawn);
    }

    @Override
    public void testForNewState() {
        if (this.canStart()) {
            this.game.transform(this.game.getFactory().starting(this.queue));
        }
    }
}
