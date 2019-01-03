package pl.themolka.janusz;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;
import pl.themolka.janusz.arena.Arena;
import pl.themolka.janusz.arena.ArenaHandler;
import pl.themolka.janusz.arena.Game;
import pl.themolka.janusz.arena.Match;

import java.util.Objects;
import java.util.Optional;

public class InstantTntHandler extends JanuszPlugin.Handler {
    public static final String IS_INSTANT_IGNITE = "is_instant_ignite";

    private final JanuszPlugin plugin;

    public InstantTntHandler(JanuszPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onTntExplode(EntityExplodeEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof TNTPrimed && entity.getMetadata(IS_INSTANT_IGNITE).stream().anyMatch(metadata -> {
            Object bool = metadata.value();
            return bool instanceof Boolean && (boolean) bool;
        })) {
            event.blockList().clear();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTntPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        if (block.getType().equals(Material.TNT) && this.canInstantIgnite(block)) {
            event.setCancelled(true);

            ItemStack inHand = event.getItemInHand();

            Player player = event.getPlayer();
            if (this.canRemoveItem(player)) {
                int amount = inHand.getAmount();
                if (amount > 1) {
                    inHand.setAmount(amount - 1);
                } else {
                    PlayerInventory inventory = player.getInventory();
                    inventory.setItem(inventory.getHeldItemSlot(), null);
                }
            }

            Location location = block.getLocation();
            location.getWorld().spawn(location, TNTPrimed.class, tnt -> {
                tnt.setCustomName(ChatColor.RED + ChatColor.BOLD.toString() + "Bomba!");
                tnt.setCustomNameVisible(true);
                tnt.setMetadata(IS_INSTANT_IGNITE, new FixedMetadataValue(this.plugin, true));
            });
        }
    }

    private boolean canRemoveItem(Player player) {
        GameMode gameMode = player.getGameMode();
        return gameMode.equals(GameMode.SURVIVAL) || gameMode.equals(GameMode.ADVENTURE);
    }

    private boolean canInstantIgnite(Block tnt) {
        Location location = Objects.requireNonNull(tnt, "tnt").getLocation();

        Optional<ArenaHandler> arenaHandler = this.plugin.getHandler(ArenaHandler.class);
        if (arenaHandler.isPresent()) {
            Game game = arenaHandler.get().getGame();
            Arena arena = game.getArena();

            return  arena.getWorldId().equals(location.getWorld().getUID()) &&
                    game.getState() instanceof Match &&
                    arena.getRegion().contains(location);
        }

        return false;
    }
}
