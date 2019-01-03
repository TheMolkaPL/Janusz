package pl.themolka.janusz.geometry;

import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class Bounds extends Cuboid {
    private final Region owner;

    public Bounds(Region owner, Vector3d min, Vector3d max) {
        super(min, max);
        this.owner = Objects.requireNonNull(owner, "owner");
    }

    @Override
    public Collection<Block> getBlocks(World world) {
        Objects.requireNonNull(world, "world");

        Vector3d min = this.getMin();
        Vector3d max = this.getMax();

        int x1 = min.getFineX(), y1 = min.getFineY(), z1 = min.getFineZ();
        int x2 = max.getFineX(), y2 = max.getFineY(), z2 = max.getFineZ();

        List<Block> blocks = new ArrayList<>((x2 - x1) * (y2 - y1) * (z2 - z1));

        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                for (int z = z1; z <= z2; z++) {
                    if (this.owner.contains(new Vector3d(x, y, z))) {
                        blocks.add(world.getBlockAt(x, y, z));
                    }
                }
            }
        }

        return blocks;
    }

    @Override
    public Bounds getBounds() {
        return this;
    }

    public Region getOwner() {
        return this.owner;
    }

    @Override
    protected Bounds createBounds() {
        return this;
    }
}
