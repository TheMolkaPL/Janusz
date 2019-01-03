package pl.themolka.janusz.geometry;

import com.google.common.collect.ForwardingObject;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Collection;
import java.util.Objects;

public class ForwardingRegion extends ForwardingObject implements Region {
    private final Region delegate;

    public ForwardingRegion(Region delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    @Override
    protected Region delegate() {
        return this.delegate;
    }

    @Override
    public boolean contains(Vector3d vector) {
        return this.delegate.contains(vector);
    }

    @Override
    public Collection<Block> getBlocks(World world) {
        return this.delegate.getBlocks(world);
    }

    @Override
    public Bounds getBounds() {
        return this.delegate.getBounds();
    }

    @Override
    public boolean contains(Location location) {
        return this.delegate.contains(location);
    }
}
