package pl.themolka.janusz.arena;

import pl.themolka.janusz.geometry.Vector3d;
import pl.themolka.janusz.profile.LocalSession;

import java.util.Objects;
import java.util.UUID;

public class DedicatedSpawn extends Spawn {
    private final LocalSession owner;

    public DedicatedSpawn(Spawn spawn, LocalSession owner) {
        this(spawn.getWorldId(), spawn.getVector3d(), spawn.getYaw(), spawn.getPitch(), owner);
    }

    public DedicatedSpawn(UUID worldId, Vector3d vector3d, float yaw, float pitch, LocalSession owner) {
        super(worldId, vector3d, yaw, pitch);
        this.owner = Objects.requireNonNull(owner, "owner");
    }

    public LocalSession getOwner() {
        return this.owner;
    }
}
