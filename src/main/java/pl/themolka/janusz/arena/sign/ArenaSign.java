package pl.themolka.janusz.arena.sign;

import org.apache.commons.lang3.Validate;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.event.player.PlayerInteractEvent;
import pl.themolka.janusz.arena.Arena;
import pl.themolka.janusz.arena.Game;
import pl.themolka.janusz.geometry.Vector3d;
import pl.themolka.janusz.profile.LocalSession;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class ArenaSign {
    private final Arena arena;
    private final Vector3d location;

    public ArenaSign(Arena arena, Vector3d location) {
        this.arena = Objects.requireNonNull(arena, "arena");
        this.location = Objects.requireNonNull(location, "location");
    }

    public Arena getArena() {
        return this.arena;
    }

    public UUID getWorldId() {
        return this.arena.getWorldId();
    }

    public Optional<World> getWorld() {
        return this.arena.getWorld();
    }

    public Vector3d getLocation() {
        return this.location;
    }

    public void click(Game game, LocalSession clicker, PlayerInteractEvent event) {
        Objects.requireNonNull(game, "game");
        Objects.requireNonNull(clicker, "clicker");
        Objects.requireNonNull(event, "event");
        Validate.isTrue(game.getArena().equals(this.arena), "Arena must match");
    }

    public void update(Game game, Sign sign) {
        Objects.requireNonNull(game, "game");
        Objects.requireNonNull(sign, "sign");
    }
}
