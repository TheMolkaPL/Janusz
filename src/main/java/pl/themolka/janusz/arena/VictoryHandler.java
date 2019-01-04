package pl.themolka.janusz.arena;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.meta.FireworkMeta;
import pl.themolka.janusz.JanuszPlugin;
import pl.themolka.janusz.arena.event.MatchEndEvent;

public class VictoryHandler extends JanuszPlugin.Handler {
    private static final FireworkEffect[] EFFECTS = new FireworkEffect[] {
            FireworkEffect.builder().with(FireworkEffect.Type.STAR)
                                    .withColor(Color.RED)
                                    .withFlicker()
                                    .withTrail()
                                    .build(),
            FireworkEffect.builder().with(FireworkEffect.Type.BALL_LARGE)
                                    .withColor(Color.ORANGE)
                                    .withFlicker()
                                    .withTrail()
                                    .build()
    };

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMatchEnd(MatchEndEvent event) {
        event.getResult().getWinner().ifPresent(winner -> this.launch(event.getArena()));
    }

    private void launch(Arena arena) {
        arena.getWorld().ifPresent(world -> arena.getTorches().forEach(torch ->
                this.launch(new Location(world, torch.getX(), torch.getY(), torch.getZ()))));
    }

    private void launch(Location at) {
        if (!at.getChunk().isLoaded()) {
            return;
        }

        Firework firework = at.getWorld().spawn(at, Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        meta.addEffects(EFFECTS);
        meta.setPower(1);
        firework.setFireworkMeta(meta);
    }
}
