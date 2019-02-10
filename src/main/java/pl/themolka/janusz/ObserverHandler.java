package pl.themolka.janusz;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Objects;
import java.util.logging.Level;

public class ObserverHandler extends JanuszPlugin.Handler {
    private final JanuszPlugin plugin;
    private final Configuration configuration;

    private Listener paperListener;

    public ObserverHandler(JanuszPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.configuration = plugin.getConfiguration();
    }

    @Override
    public void enable(JanuszPlugin plugin) {
        super.enable(plugin);

        try {
            Class.forName("com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent");
            this.plugin.registerEvents(this.paperListener = new ObserverPaperListener(this));
            plugin.getLogger().log(Level.INFO, "Hooked to the Paper API.");
        } catch (ClassNotFoundException ignored) {
            plugin.getLogger().log(Level.INFO, "Could not hook to the Paper API.");
        }
    }

    @Override
    public void disable(JanuszPlugin plugin) {
        if (this.paperListener != null) {
            plugin.unregisterEvents(this.paperListener);
        }

        super.disable(plugin);
    }

    public boolean isObserving(Entity entity) {
        return entity instanceof Player && this.isObserving((Player) entity);
    }

    public boolean isObserving(Player player) {
        return this.configuration.getObserverNames().stream()
                .anyMatch(observerName -> observerName.equalsIgnoreCase(player.getName()));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (this.isObserving(event.getEntity())) {
            event.setCancelled(true);

            if (event instanceof EntityDamageByEntityEvent) {
                Player player = null;

                Entity damager = ((EntityDamageByEntityEvent) event).getDamager();
                if (damager instanceof Projectile) {
                    ProjectileSource source = ((Projectile) damager).getShooter();
                    if (source instanceof Player) {
                        player = (Player) source;
                    }
                } else if (damager instanceof Player) {
                    player = (Player) damager;
                }

                if (player != null) {
                    player.sendMessage(ChatColor.RED + "Nie bij mnie :(");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (this.isObserving(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityTarget(EntityTargetEvent event) {
        if (this.isObserving(event.getTarget())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerExpChange(PlayerExpChangeEvent event) {
        if (this.isObserving(event.getPlayer())) {
            event.setAmount(0);
        }
    }
}
