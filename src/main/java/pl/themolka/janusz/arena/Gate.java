package pl.themolka.janusz.arena;

import org.bukkit.Material;
import org.bukkit.World;
import pl.themolka.janusz.geometry.Region;

import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

public class Gate {
    public static final Material OPENED = Material.AIR;
    public static final Material CLOSED = Material.BARRIER;

    private final Arena arena;
    private final Logger logger;
    private final Region region;

    private boolean open;

    public Gate(Arena arena, Logger logger, Region region) {
        this.arena = Objects.requireNonNull(arena, "arena");
        this.logger = Objects.requireNonNull(logger, "logger");
        this.region = Objects.requireNonNull(region, "region");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Gate gate = (Gate) o;
        return  Objects.equals(arena, gate.arena) &&
                Objects.equals(region, gate.region);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arena, region);
    }

    public Arena getArena() {
        return this.arena;
    }

    public Region getRegion() {
        return this.region;
    }

    public boolean isOpen() {
        return this.open;
    }

    public Optional<World> getWorld() {
        return this.arena.getWorld();
    }

    public void open() {
        this.logger.fine("Opening " + this.oldState() + " gate at " + this.region.toString());
        this.changeBlocks(this.getWorld().orElseThrow(NullPointerException::new), OPENED);
        this.open = true;
    }

    public void close() {
        this.logger.fine("Closing " + this.oldState() + " gate at " + this.region.toString());
        this.changeBlocks(this.getWorld().orElseThrow(NullPointerException::new), CLOSED);
        this.open = false;
    }

    protected String oldState() {
        return this.open ? "opened" : "closed";
    }

    protected void changeBlocks(World world, Material material) {
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(material, "material");
        this.region.getBlocks(world).forEach(block -> block.setType(material));
    }
}
