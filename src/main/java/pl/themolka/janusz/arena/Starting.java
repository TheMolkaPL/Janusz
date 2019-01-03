package pl.themolka.janusz.arena;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.Validate;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;
import pl.themolka.janusz.profile.LocalSession;

import java.util.Map;
import java.util.Set;

public class Starting extends GameState.Queue {
    private static final int DEFAULT_COOLDOWN = 5;

    private static final Map<Integer, String> POLISH_SECONDS = ImmutableMap.<Integer, String>builder()
            .put(1, "sekundę")
            .put(2, "sekundy")
            .put(3, "sekundy")
            .put(4, "sekundy")
            .put(5, "sekund")
            .build();

    private final Countdown countdown;
    private final DedicatedSpawnLibrary library;

    public Starting(Game game, Set<LocalSession> queue, Set<Spawn> spawns) {
        super(game, queue);
        Validate.isTrue(this.canStart(), "Competitor queue not ready");

        this.countdown = new Countdown(DEFAULT_COOLDOWN);
        this.library = new DedicatedSpawnLibrary(spawns);
    }

    @Override
    public void enableState() {
        this.queue.forEach(competitor -> this.library.resolve(competitor)
                .ifPresent(spawn -> spawn.spawn(competitor)));

        this.countdown.runTaskTimer(this.plugin, 0L, 20L);
    }

    @Override
    public void disableState() {
        this.countdown.cancel();
    }

    @Override
    public void testForNewState() {
        if (!this.canStart()) {
            // sorry!
            Spawn defaultSpawn = this.game.getArena().getDefaultSpawn();
            this.queue.forEach(defaultSpawn::spawn);

            this.game.transform(this.game.getFactory().idle(this.queue));
        }
    }

    private void begin() {
        this.reloadQueue(); // to make sure...

        GameStateFactory factory = this.game.getFactory();
        this.game.transform(this.canStart() ? factory.match(this.queue)
                                            : factory.idle(this.queue));
    }

    private void announce(int seconds) {
        this.game.getArena().getWorld().ifPresent(world -> this.plugin.getServer().getOnlinePlayers().stream()
                .filter(player -> player.getWorld().equals(world))
                .forEach(player -> {
                    this.logger.info("Starting the match in " + seconds + " seconds...");
                    if (seconds != 0) {
                        player.sendMessage(ChatColor.GREEN + "Bitwa na arenie PVP rozpocznie się za " + ChatColor.DARK_GREEN +
                                seconds + " " + POLISH_SECONDS.getOrDefault(seconds, "sekund") + ChatColor.GREEN + "...");
                    }

                    if (this.isQueued(player.getUniqueId())) {
                        String text = seconds != 0 ? ChatColor.GREEN + Integer.toString(seconds)
                                                   : ChatColor.RED + ChatColor.UNDERLINE.toString() + "GO!";
                        player.sendTitle(text, "", 10, 3, 30);
                    }
                }));
    }

    class Countdown extends BukkitRunnable {
        private int left; // in seconds

        Countdown(int seconds) {
            Validate.isTrue(seconds > 0, "seconds must be positive");
            this.left = seconds;
        }

        @Override
        public void run() {
            announce(this.left);

            if (this.left <= 0) {
                begin();
                return;
            }

            this.left--;
        }
    }
}
