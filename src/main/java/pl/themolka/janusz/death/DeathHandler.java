package pl.themolka.janusz.death;

import net.minecraft.server.v1_13_R2.EntityTypes;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import pl.themolka.janusz.JanuszPlugin;
import pl.themolka.janusz.database.Database;
import pl.themolka.janusz.geometry.Vector3d;
import pl.themolka.janusz.profile.LocalSession;
import pl.themolka.janusz.profile.LocalSessionHandler;
import pl.themolka.janusz.season.Season;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

public class DeathHandler extends JanuszPlugin.Handler {
    private final JanuszPlugin plugin;
    private final Database database;

    private final DeathDao deathDao;

    public DeathHandler(JanuszPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.database = plugin.getDb();

        this.deathDao = this.database.getDeathDao();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void logPlayerDeathInDatabase(PlayerDeathEvent event) {
        LocalSessionHandler localSessionHandler = this.plugin.getHandler(LocalSessionHandler.class).orElse(null);
        if (localSessionHandler == null) {
            return;
        }

        Season season = this.plugin.getSeasons().current();
        Player victimBukkit = event.getEntity();

        LocalSession victim = localSessionHandler.getLocalSession(victimBukkit).orElse(null);
        if (victim == null) {
            return;
        }

        Location bukkitLocation = victimBukkit.getLocation();
        String world = bukkitLocation.getWorld().getName();
        Vector3d location = new Vector3d(bukkitLocation.getX(), bukkitLocation.getY(), bukkitLocation.getZ());
        float fallDistance = victimBukkit.getFallDistance();

        EntityDamageEvent lastDamage = victimBukkit.getLastDamageCause();

        String cause = Optional.ofNullable(lastDamage)
                .map(last -> last.getCause().name().toLowerCase())
                .orElse(null);

        Killer killer = null;
        if (lastDamage instanceof EntityDamageByEntityEvent) {
            Entity damager = ((EntityDamageByEntityEvent) lastDamage).getDamager();
            String type = Objects.requireNonNull(EntityTypes.getName(((CraftEntity) damager).getHandle().P())).toString();
            // ^ HOW the heck can I get the entity type namespace and ID??

            killer = Optional.ofNullable(localSessionHandler.getLocalSession(damager))
                    .filter(Optional::isPresent)
                    .map(sessionMaybe -> (Killer) new PlayerKiller(sessionMaybe.get()))
                    .orElseGet(() -> new Killer(type));
        }

        Death death = new Death(LocalDateTime.now(),
                                season,
                                false,
                                victim,
                                world,
                                location,
                                cause,
                                fallDistance,
                                killer);

        this.database.getExecutor().submit(() -> this.deathDao.save(death));
    }
}
