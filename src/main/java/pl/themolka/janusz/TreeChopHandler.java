package pl.themolka.janusz;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class TreeChopHandler extends JanuszPlugin.Handler {
    private static final Vector NO_VELOCITY = new Vector();

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onTreeChop(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        if (this.canChop(player)) {
            ChopSession session = new ChopSession();
            session.recursive(player, block);
        }
    }

    private boolean isWood(Material material) {
        switch (material) {
            case ACACIA_LOG:
            case BIRCH_LOG:
            case DARK_OAK_LOG:
            case JUNGLE_LOG:
            case OAK_LOG:
            case SPRUCE_LOG:
                return true;
            default:
                return false;
        }
    }

    private boolean isTool(Material material) {
        switch (material) {
            case WOODEN_AXE:
            case STONE_AXE:
            case GOLDEN_AXE:
            case IRON_AXE:
            case DIAMOND_AXE:
                return true;
            default:
                return false;
        }
    }

    private boolean canChop(Player player) {
        if (player.isSneaking()) {
            return false;
        }

        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool == null || !this.isTool(tool.getType())) {
            return false;
        }

        switch (player.getGameMode()) {
            case SURVIVAL:
            case ADVENTURE:
                return true;
        }

        return false;
    }

    class ChopSession {
        int broken = 0;

        void recursive(Player player, Block base) {
            if (this.broken >= 50) {
                player.sendMessage(ChatColor.RED + "Za duże drzewo! :(");
                return;
            }

            for (BlockFace face : BlockFace.values()) {
                if (face.equals(BlockFace.DOWN)) {
                    continue;
                }

                Block block = base.getRelative(face);

                if (this.chop(block)) {
                    this.broken++;
                    this.recursive(player, block);
                }
            }
        }

        boolean chop(Block block) {
            if (!isWood(block.getType())) {
                return false;
            }

            Material material = block.getType();
            block.setType(Material.AIR, true);

            Item item = block.getWorld().dropItem(block.getLocation(), new ItemStack(material, 1));
            item.setVelocity(NO_VELOCITY);
            return true;
        }
    }
}
