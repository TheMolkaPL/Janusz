package pl.themolka.janusz.arena;

import org.apache.commons.lang3.Validate;
import org.bukkit.World;
import pl.themolka.janusz.JanuszPlugin;
import pl.themolka.janusz.arena.sign.JoinSign;
import pl.themolka.janusz.geometry.Region;
import pl.themolka.janusz.geometry.Vector3d;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class Arena {
    protected final JanuszPlugin plugin;

    private final String name;

    private final UUID worldId;
    private final Region field;
    private final Region region;
    private final Spawn defaultSpawn;

    private final Set<Gate> gates = new HashSet<>();
    private final Set<Spawn> spawns = new HashSet<>();
    private final Set<Vector3d> torches = new HashSet<>();
    private final Set<JoinSign> joinSigns = new HashSet<>();

    public Arena(JanuszPlugin plugin, String name, UUID worldId, Region field, Region region, Spawn defaultSpawn) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");

        this.name = Objects.requireNonNull(name, "uniqueId");

        int nameLength = this.name.length();
        Validate.isTrue(nameLength >= 3 && nameLength <= 16, "name too short or too long");

        this.worldId = Objects.requireNonNull(worldId, "worldId");
        this.field = Objects.requireNonNull(field, "field");
        this.region = Objects.requireNonNull(region, "region");
        this.defaultSpawn = Objects.requireNonNull(defaultSpawn, "defaultSpawn");
    }

    public boolean addGate(Gate gate) {
        return this.gates.add(Objects.requireNonNull(gate, "gate"));
    }

    public boolean addSpawn(Spawn spawn) {
        return this.spawns.add(Objects.requireNonNull(spawn, "spawn"));
    }

    public boolean addTorch(Vector3d torch) {
        return this.torches.add(Objects.requireNonNull(torch, "torch"));
    }

    public boolean addJoinSign(JoinSign joinSign) {
        return this.joinSigns.add(Objects.requireNonNull(joinSign, "joinSign"));
    }

    public String getName() {
        return this.name;
    }

    public UUID getWorldId() {
        return this.worldId;
    }

    public Optional<World> getWorld() {
        return Optional.ofNullable(this.plugin.getServer().getWorld(this.worldId));
    }

    public Region getField() {
        return this.field;
    }

    public Region getRegion() {
        return this.region;
    }

    public Spawn getDefaultSpawn() {
        return this.defaultSpawn;
    }

    public Set<Gate> getGates() {
        return new HashSet<>(this.gates);
    }

    public Set<Spawn> getSpawns() {
        return new HashSet<>(this.spawns);
    }

    public Set<Vector3d> getTorches() {
        return new HashSet<>(this.torches);
    }

    public Set<JoinSign> getJoinSigns() {
        return new HashSet<>(this.joinSigns);
    }

    public boolean removeGate(Gate gate) {
        return this.gates.remove(Objects.requireNonNull(gate, "gate"));
    }

    public boolean removeSpawn(Spawn spawn) {
        return this.spawns.remove(Objects.requireNonNull(spawn, "spawn"));
    }

    public boolean removeTorch(Vector3d torch) {
        return this.torches.remove(Objects.requireNonNull(torch, "torch"));
    }

    public boolean removeJoinSign(JoinSign joinSign) {
        return this.joinSigns.remove(Objects.requireNonNull(joinSign, "joinSign"));
    }
}
