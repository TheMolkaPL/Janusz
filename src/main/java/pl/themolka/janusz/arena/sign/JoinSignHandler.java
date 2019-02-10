package pl.themolka.janusz.arena.sign;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import pl.themolka.janusz.JanuszPlugin;
import pl.themolka.janusz.arena.ArenaHandler;
import pl.themolka.janusz.arena.Game;
import pl.themolka.janusz.arena.event.GameStateChangeEvent;
import pl.themolka.janusz.geometry.Vector3d;
import pl.themolka.janusz.profile.LocalSessionHandler;

import java.util.Objects;
import java.util.Optional;

public class JoinSignHandler extends JanuszPlugin.Handler {
    private final JanuszPlugin plugin;

    public JoinSignHandler(JanuszPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    @Override
    public void enable(JanuszPlugin plugin) {
        super.enable(plugin);

        plugin.getHandler(ArenaHandler.class).ifPresent(handler -> {
            Game game = handler.getGame();
            game.getArena().getJoinSigns().forEach(join -> this.update(game, join));
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void updateWhenStateHasChanged(GameStateChangeEvent event) {
        Game game = event.getGame();
        game.getArena().getJoinSigns().forEach(join -> this.update(game, join));
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void joinPlayerWhenClicked(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!this.canJoin(player)) {
            return;
        }

        Block clicked = event.getClickedBlock();
        if (clicked == null) {
            return;
        }

        LocalSessionHandler sessionHandler = this.plugin.getHandler(LocalSessionHandler.class).orElse(null);
        if (sessionHandler == null) {
            return;
        }

        sessionHandler.getLocalSession(player).ifPresent(clicker -> this.findSign(clicked.getLocation()).ifPresent(pair -> {
            if (player.hasPermission("janusz.join-arena")) {
                pair.getRight().click(pair.getLeft(), clicker, event);
            } else {
                clicker.printError("Brak odpowiednich uprawnień");
            }
        }));
    }

    private boolean canJoin(Player clicker) {
        return !Objects.requireNonNull(clicker, "clicker").getGameMode().equals(GameMode.SPECTATOR);
    }

    private Optional<Pair<Game, JoinSign>> findSign(Location at) {
        Objects.requireNonNull(at, "at");

        ArenaHandler handler = this.plugin.getHandler(ArenaHandler.class).orElse(null);
        if (handler == null) {
            return Optional.empty();
        }

        Game game = handler.getGame();
        return game.getArena().getJoinSigns().stream()
                .filter(joinSign -> Objects.equals(this.getLocation(joinSign), at))
                .findFirst()
                .map(join -> ImmutablePair.of(game, join));
    }

    private void update(Game game, JoinSign join) {
        Objects.requireNonNull(game, "game");
        Objects.requireNonNull(join, "join");

        Location location = this.getLocation(join);
        if (location != null) {
            BlockState state = location.getBlock().getState();
            if (state instanceof Sign) {
                join.update(game, (Sign) state);
                state.update(true, false);
            }
        }
    }

    private Location getLocation(JoinSign sign) {
        World world = Objects.requireNonNull(sign, "sign").getWorld().orElse(null);
        if (world != null) {
            Vector3d vector = sign.getLocation();
            return world.getBlockAt(vector.getFineX(), vector.getFineY(), vector.getFineZ()).getLocation();
        }

        return null;
    }
}
