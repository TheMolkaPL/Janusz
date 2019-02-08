package pl.themolka.janusz.util;

import com.google.common.collect.ImmutableSet;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.Objects;
import java.util.Optional;

public class SecureSpawn {
    private static final ImmutableSet<Material> AIR = ImmutableSet.of(Material.AIR,
                                                                      Material.CAVE_AIR,
                                                                      Material.VOID_AIR);

    private final Location spawn;

    public SecureSpawn(Location spawn) {
        this.spawn = Objects.requireNonNull(spawn, "spawn");
    }

    public boolean isSecure() {
        return this.isSecure(this.spawn);
    }

    public Optional<Location> resolveSecure() {
        if (this.spawn.getWorld() == null) {
            return Optional.empty();
        } else if (this.isSecure()) {
            return Optional.of(this.spawn);
        }

        Location highest = this.spawn.getWorld().getHighestBlockAt(this.spawn).getLocation();
        if (this.isSecure(highest)) {
            return Optional.of(highest);
        }

        return Optional.empty();
    }

    private boolean isSecure(Location what) {
        Objects.requireNonNull(what, "what");

        Location head = what.clone().add(0D, 1D, 0D);
        Location legs = what.clone();
        Location under = what.clone().subtract(0D, 1D, 0D);

        return  this.isAir(head.getBlock()) &&
                this.isAir(legs.getBlock()) &&
                this.isSecureUnder(under.getBlock());
    }

    private boolean isAir(Block block) {
        return this.isAir(Objects.requireNonNull(block, "block").getType());
    }

    private boolean isAir(Material material) {
        return AIR.contains(Objects.requireNonNull(material, "material"));
    }

    private boolean isSecureUnder(Block block) {
        return this.isSecureUnder(Objects.requireNonNull(block, "block").getType());
    }

    private boolean isSecureUnder(Material material) {
        return Objects.requireNonNull(material, "material").isSolid();
    }
}
