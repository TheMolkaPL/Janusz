package pl.themolka.janusz.arena.event;

import pl.themolka.janusz.JanuszEvent;
import pl.themolka.janusz.JanuszPlugin;
import pl.themolka.janusz.arena.Arena;

import java.util.Objects;

public abstract class ArenaEvent extends JanuszEvent {
    private final Arena arena;

    public ArenaEvent(JanuszPlugin plugin, Arena arena) {
        super(plugin);
        this.arena = Objects.requireNonNull(arena, "arena");
    }

    public ArenaEvent(boolean isAsync, JanuszPlugin plugin, Arena arena) {
        super(isAsync, plugin);
        this.arena = Objects.requireNonNull(arena, "arena");
    }

    public Arena getArena() {
        return this.arena;
    }
}
