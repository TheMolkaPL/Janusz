package pl.themolka.janusz.util;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

public final class ChunkUtils {
    private ChunkUtils() {
    }

    public static Set<Block> searchBlocks(Chunk chunk, Predicate<Block> filter) {
        Objects.requireNonNull(chunk, "chunk");
        Objects.requireNonNull(filter, "filter");

        if (!chunk.isLoaded()) {
            throw new IllegalStateException("Chunk is not loaded");
        }

        Set<Block> blocks = new HashSet<>(16 * 256 * 16);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < 256; y++) {
                    Block block = chunk.getBlock(x, y, z);

                    if (filter.test(block)) {
                        blocks.add(block);
                    }
                }
            }
        }

        return blocks;
    }

    public static Set<Block> searchBlocks(Chunk chunk, Material material) {
        Objects.requireNonNull(material, "material");
        return searchBlocks(chunk, block -> Objects.equals(block.getType(), material));
    }
}
