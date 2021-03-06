package pl.themolka.janusz;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Fixing game mode changing by the Multiverse-Core plugin.
 */
public class GameModeFixerHandler extends JanuszPlugin.Handler {
    private final Cache<UUID, GameMode> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(1L, TimeUnit.SECONDS)
            .build();

    private final JanuszPlugin plugin;
    private final Configuration configuration;

    public GameModeFixerHandler(JanuszPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.configuration = plugin.getConfiguration();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoinAtLowest(PlayerJoinEvent lowest) {
        if (this.configuration.getGameModeFixer()) {
            Player joiner = lowest.getPlayer();
            this.cache.put(joiner.getUniqueId(), joiner.getGameMode());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoinAtHighest(PlayerJoinEvent highest) {
        if (this.configuration.getGameModeFixer()) {
            Player joiner = highest.getPlayer();
            Optional.ofNullable(this.cache.getIfPresent(joiner.getUniqueId()))
                    .ifPresent(joiner::setGameMode);
        }
    }
}
