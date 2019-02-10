package pl.themolka.janusz;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.Objects;

public class TreeChopHandler extends JanuszPlugin.Handler {
    private static final Vector NO_VELOCITY = new Vector();

    private final JanuszPlugin plugin;
    private final Configuration configuration;

    public TreeChopHandler(JanuszPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.configuration = plugin.getConfiguration();
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onTreeChop(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        ItemStack tool = player.getInventory().getItemInMainHand();

        if (this.canChop(player, tool)) {
            ChopSession session = new ChopSession(player, block, tool);
            session.begin(block);
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

    private boolean canChop(Player player, ItemStack tool) {
        if (player.isSneaking()) {
            return false;
        } else if (tool == null || !this.isTool(tool.getType())) {
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
        final Player player;
        final Block base;
        final ItemStack tool;

        int broken = 0;

        ChopSession(Player player, Block base, ItemStack tool) {
            this.player = Objects.requireNonNull(player, "player");
            this.base = Objects.requireNonNull(base, "base");
            this.tool = tool;
        }

        void begin(Block base) {
            if (this.chop(base)) { // this block
                this.broken++;
                this.recursive(base); // blocks around
            }
        }

        /**
         * Chop blocks around the given {@code base}
         */
        void recursive(Block base) {
            if (this.broken >= configuration.getTreeSizeLimit()) {
                this.player.sendMessage(ChatColor.RED + "Za duże drzewo! :(");
                return;
            }

            for (BlockFace face : BlockFace.values()) {
                if (face.equals(BlockFace.DOWN)) {
                    continue;
                }

                this.begin(base.getRelative(face));
            }
        }

        boolean chop(Block block) {
            if (!canChop(block)) {
                return false;
            }

            ItemStack itemStack = new ItemStack(block.getType(), 1);

            PlayerInventory inventory = this.player.getInventory();
            if (canReceive(inventory, itemStack.getType(), itemStack.getAmount())) {
                this.player.getInventory().addItem(itemStack);
            } else {
                Item item = block.getWorld().dropItem(block.getLocation(), itemStack);
                item.setVelocity(NO_VELOCITY.clone());
            }

            block.setType(Material.AIR, true);

            if (this.tool != null && this.damage(this.tool)) {
                inventory.remove(this.tool);
                this.player.playSound(this.player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1F, 1F);
            }

            return true;
        }

        boolean damage(ItemStack tool) {
            ItemMeta meta = tool.getItemMeta();
            if (meta instanceof Damageable) {
                Damageable damageable = (Damageable) meta;
                damageable.setDamage(damageable.getDamage() + 1);
                tool.setItemMeta(meta);

                return tool.getDurability() >= tool.getType().getMaxDurability();
            }

            return false;
        }
    }
}
