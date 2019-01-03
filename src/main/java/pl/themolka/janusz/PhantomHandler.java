package pl.themolka.janusz;

import org.apache.commons.lang3.Validate;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.Objects;

public class PhantomHandler extends JanuszPlugin.Handler {
    private static final EntityType PHANTOM = EntityType.PHANTOM;

    private final JanuszPlugin plugin;
    private final Configuration configuration;

    private final double chance;

    public PhantomHandler(JanuszPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.configuration = plugin.getConfiguration();

        this.chance = this.configuration.getPhantomSpawnChance();
        Validate.isTrue(0D <= this.chance && this.chance <= 1D, "Invalid chance");
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getEntity().getType().equals(PHANTOM) &&
                event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.NATURAL)) {
            if (Math.random() > this.chance) {
                event.setCancelled(true);
            }
        }
    }
}
