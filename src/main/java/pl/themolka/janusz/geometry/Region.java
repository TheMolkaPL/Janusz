package pl.themolka.janusz.geometry;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Collection;
import java.util.Objects;

public interface Region {
    boolean contains(Vector3d vector);

    default Collection<Block> getBlocks(World world) {
        return this.getBounds().getBlocks(world);
    }

    Bounds getBounds();

    default boolean contains(Location location) {
        Objects.requireNonNull(location, "location");
        return this.contains(new Vector3d(location.getX(), location.getY(), location.getZ()));
    }
}
