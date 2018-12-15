package pl.themolka.janusz;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_13_R2.block.CraftBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

public class TreeChopHandler extends JanuszPlugin.Handler {
    private static final Vector NO_VELOCITY = new Vector();

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onTreeChop(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        if (this.canChop(player)) {
            ChopSession session = new ChopSession();
            session.begin(player, block);
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

    private boolean canChop(Block block) {
        return this.isWood(block.getType());
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

    private boolean canReceive(PlayerInventory inventory, Material material, int amount) {
        int space = 0;

        for (ItemStack item : inventory.getContents()) {
            Material kind = item != null ? item.getType() : null;
            if (kind == null || kind.equals(Material.AIR) || kind.equals(material)) {
                space += material.getMaxStackSize() - (item != null ? item.getAmount() : 0);
            }

            if (space >= amount) {
                return true;
            }
        }

        return false;
    }

    class ChopSession {
        int broken = 0;

        void begin(Player player, Block base) {
            if (this.chop(player, base)) { // this block
                this.broken++;
                this.recursive(player, base); // blocks around
            }
        }

        /**
         * Chop blocks around the given {@code base}
         */
        void recursive(Player player, Block base) {
            if (this.broken >= 50) {
                player.sendMessage(ChatColor.RED + "Za duże drzewo! :(");
                return;
            }

            for (BlockFace face : BlockFace.values()) {
                if (face.equals(BlockFace.DOWN)) {
                    continue;
                }

                this.begin(player, base.getRelative(face));
            }
        }

        boolean chop(Player player, Block block) {
            if (!canChop(block)) {
                return false;
            }

            ItemStack itemStack = new ItemStack(block.getType(), 1);

            PlayerInventory inventory = player.getInventory();
            if (canReceive(inventory, itemStack.getType(), itemStack.getAmount())) {
                player.getInventory().addItem(itemStack);
            } else {
                Item item = block.getWorld().dropItem(block.getLocation(), itemStack);
                item.setVelocity(NO_VELOCITY.clone());
            }

            block.setType(Material.AIR, true);
            return true;
        }
    }
}
