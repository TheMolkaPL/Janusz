package pl.themolka.janusz.arena;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.event.player.PlayerTeleportEvent;
import pl.themolka.janusz.geometry.Vector3d;
import pl.themolka.janusz.profile.LocalSession;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class Spawn {
    private static final PlayerTeleportEvent.TeleportCause CAUSE = PlayerTeleportEvent.TeleportCause.PLUGIN;

    private final UUID worldId;
    private final Vector3d vector3d;
    private final float yaw;
    private final float pitch;

    public Spawn(UUID worldId, Vector3d vector3d, float yaw, float pitch) {
        this.worldId = Objects.requireNonNull(worldId, "worldId");
        this.vector3d = Objects.requireNonNull(vector3d, "vector3d");
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Spawn spawn = (Spawn) o;
        return Float.compare(spawn.yaw, yaw) == 0 &&
                Float.compare(spawn.pitch, pitch) == 0 &&
                Objects.equals(worldId, spawn.worldId) &&
                Objects.equals(vector3d, spawn.vector3d);
    }

    @Override
    public int hashCode() {
        return Objects.hash(worldId, vector3d, yaw, pitch);
    }

    public UUID getWorldId() {
        return this.worldId;
    }

    public Vector3d getVector3d() {
        return this.vector3d;
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public Optional<Location> toBukkit(Server server) {
        Objects.requireNonNull(server, "server");
        return Optional.ofNullable(server.getWorld(this.worldId)).map(world -> {
            Vector3d vector = this.vector3d;
            return new Location(world, vector.getX(), vector.getY(), vector.getZ(), this.yaw, this.pitch);
        });
    }

    public void spawn(LocalSession competitor) {
        Server server = Objects.requireNonNull(competitor, "competitor").getServer();
        Location location = this.toBukkit(server).orElseThrow(NullPointerException::new);
        competitor.getBukkit().ifPresent(player -> player.teleport(location, CAUSE));
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("worldId", worldId)
                .append("vector3d", vector3d)
                .append("yaw", yaw)
                .append("pitch", pitch)
                .toString();
    }
}
