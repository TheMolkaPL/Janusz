package pl.themolka.janusz.arena;

import pl.themolka.janusz.JanuszPlugin;
import pl.themolka.janusz.profile.LocalSession;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class GameState {
    protected final JanuszPlugin plugin;
    protected final Game game;
    protected final Logger logger;

    public GameState(Game game) {
        this.plugin = Objects.requireNonNull(game, "game").plugin;
        this.game = game;
        this.logger = game.logger;
    }

    public Game getGame() {
        return this.game;
    }

    public void enableState() {
    }

    public void disableState() {
    }

    protected boolean canJoin(LocalSession competitor) {
        return false;
    }

    public boolean isRunning() {
        return false;
    }

    protected boolean join(LocalSession competitor) {
        return false;
    }

    protected Optional<MatchResult> leave(LocalSession competitor) {
        return Optional.empty();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    public static class Queue extends GameState {
        protected final Set<LocalSession> queue = new HashSet<>();

        public Queue(Game game, Set<LocalSession> queue) {
            super(game);
            if (queue != null) {
                this.queue.addAll(queue);
            }
        }

        public boolean isQueued(UUID uniqueId) {
            Objects.requireNonNull(uniqueId, "uniqueId");
            return this.queue.stream()
                    .anyMatch(competitor -> competitor.getUniqueId().equals(uniqueId));
        }

        public boolean canStart() {
            return this.queue.size() >= this.game.getMinPlayerCount();
        }

        /**
         * In strange case when competitor is only a ghost.
         * This should actually never happen.
         */
        public void reloadQueue() {
            this.queue.retainAll(this.queue.stream()
                    .filter(LocalSession::isOnline)
                    .collect(Collectors.toList()));
        }

        @Override
        protected boolean canJoin(LocalSession competitor) {
            Objects.requireNonNull(competitor, "competitor");

            if (this.isQueued(competitor.getUniqueId())) {
                competitor.printError("Już jesteś w kolejce! Zaczekaj spokojnie na grę. =)");
                return false;
            }

            this.reloadQueue();

            if (this.queue.size() >= this.game.getArena().getSpawns().size()) {
                competitor.printError("Gra jest pełna! Spróbuj ponownie później. =)");
                return false;
            }

            return true;
        }

        @Override
        protected boolean join(LocalSession competitor) {
            Objects.requireNonNull(competitor, "competitor");
            this.reloadQueue();

            boolean ok;
            if (ok = this.queue.add(competitor)) {
                this.testForNewState();
            }
            return ok;
        }

        @Override
        protected Optional<MatchResult> leave(LocalSession competitor) {
            Objects.requireNonNull(competitor, "competitor");
            this.reloadQueue();

            if (this.queue.remove(competitor)) {
                this.testForNewState();
            }

            return Optional.empty();
        }

        protected void testForNewState() {
        }
    }
}
