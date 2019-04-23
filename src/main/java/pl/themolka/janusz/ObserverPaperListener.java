package pl.themolka.janusz;

import com.destroystokyo.paper.event.entity.PhantomPreSpawnEvent;
import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Objects;

public class ObserverPaperListener implements Listener {
    private final ObserverHandler handler;

    public ObserverPaperListener(ObserverHandler handler) {
        this.handler = Objects.requireNonNull(handler, "handler");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onPlayerPickupExperience(PlayerPickupExperienceEvent event) {
        if (this.handler.isObserving(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPhantomPreSpawn(PhantomPreSpawnEvent event) {
        if (this.handler.isObserving(event.getSpawningEntity())) {
            event.setCancelled(true);
        }
    }
}
