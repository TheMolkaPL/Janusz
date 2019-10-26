package pl.themolka.janusz;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Door;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.themolka.janusz.profile.LocalSession;
import pl.themolka.janusz.util.ChunkUtils;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CloseDoorsCommand extends JanuszPlugin.CommandHandler {
    public CloseDoorsCommand() {
        super("closedoors");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(LocalSession.ERROR + "Nie jesteś graczem!");
            return true;
        }

        Player player = (Player) sender;
        Set<Chunk> loadedChunks = Stream.of(player.getWorld().getLoadedChunks())
                .filter(Chunk::isLoaded)
                .collect(Collectors.toSet());

        sender.sendMessage(ChatColor.YELLOW + "Próba zamknięcia wszystkich drzwi na " + loadedChunks.size() + " chunkach...");

        int totalDoors = 0;
        int totalChunks = 0;

        for (Chunk chunk : loadedChunks) {
            Set<Block> blocks = ChunkUtils.searchBlocks(chunk, block -> {
                switch (block.getType()) {
                    case ACACIA_DOOR:
                    case BIRCH_DOOR:
                    case DARK_OAK_DOOR:
                    case JUNGLE_DOOR:
                    case OAK_DOOR:
                    case SPRUCE_DOOR:
                        return true;
                    default:
                        return false;
                }
            });

            int wasClosed = totalDoors;

            for (Block block : blocks) {
                BlockData data = block.getBlockData();
                if (!(data instanceof Door)) {
                    continue;
                }

                Door door = (Door) data;
                if (door.isOpen()) {
                    door.setOpen(false);
                    block.setBlockData(door, false);

                    if (door.getHalf().equals(Bisected.Half.TOP)) {
                        totalDoors++;
                    }
                }
            }

            if (wasClosed != totalDoors) {
                totalChunks++;
            }
        }

        sender.sendMessage(LocalSession.SUCCESS + "Zamknięto " + totalDoors + " drzwi na " + totalChunks + " chunkach.");
        return true;
    }
}
