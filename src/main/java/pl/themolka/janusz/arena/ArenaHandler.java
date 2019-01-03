package pl.themolka.janusz.arena;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import pl.themolka.janusz.JanuszPlugin;
import pl.themolka.janusz.arena.event.MatchEndEvent;
import pl.themolka.janusz.arena.sign.JoinSign;
import pl.themolka.janusz.database.Database;
import pl.themolka.janusz.geometry.Cuboid;
import pl.themolka.janusz.geometry.Cylinder;
import pl.themolka.janusz.geometry.Region;
import pl.themolka.janusz.geometry.Vector3d;

import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;

public class ArenaHandler extends JanuszPlugin.Handler {
    private final JanuszPlugin plugin;
    private final Database database;

    private final MatchResultDao matchResultDao;

    private final Game game;

    public ArenaHandler(JanuszPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.database = plugin.getDb();

        this.matchResultDao = this.database.getMatchResultDao();

        this.game = new Game(plugin, this.plugin.getLogger(), this.createDefaultArena(), 2);
    }

    @Override
    public void enable(JanuszPlugin plugin) {
        super.enable(plugin);
        this.game.getArena().getGates().forEach(Gate::close);
    }

    public Game getGame() {
        return this.game;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void logMatchResultInDatabase(MatchEndEvent event) {
        MatchResult result = event.getResult();
        if (result.getWinner().isPresent()) {
            this.database.getExecutor().submit(() -> this.matchResultDao.save(result));
        }
    }

    private Arena createDefaultArena() {
        Logger logger = this.plugin.getLogger();
        logger.info("Creating default arena...");

        UUID worldId = this.plugin.getServer().getWorlds().get(0).getUID();
        Region region = new Cylinder(new Vector3d(-514.5D, 60D, 1292.5D), 30.5D, 8D);
        Spawn defaultSpawn = new Spawn(worldId, new Vector3d(-543.5D, 69D, 1292.5D), -90F, 0F);

        Arena arena = new Arena(this.plugin, "Arena PVP", worldId, region, defaultSpawn);

        arena.addSpawn(new Spawn(worldId, new Vector3d(-542.5D, 63D, 1292.5D), -90F, 0F)); // west
        arena.addSpawn(new Spawn(worldId, new Vector3d(-486.5D, 63D, 1292.5D), +90F, 0F)); // east

        arena.addGate(new Gate(arena, logger, new Cuboid(new Vector3d(-490, 63, 1291), new Vector3d(-490, 65, 1293)))); // west
        arena.addGate(new Gate(arena, logger, new Cuboid(new Vector3d(-540, 63, 1291), new Vector3d(-540, 65, 1293)))); // east

        arena.addTorch(new Vector3d(-548.5, 74, 1258.5));
        arena.addTorch(new Vector3d(-480.5, 74, 1258.5));
        arena.addTorch(new Vector3d(-548.5, 74, 1326.5));
        arena.addTorch(new Vector3d(-480.5, 74, 1326.5));

        arena.addJoinSign(new JoinSign(arena, new Vector3d(-542, 70, 1290)));
        arena.addJoinSign(new JoinSign(arena, new Vector3d(-541, 70, 1289)));
        arena.addJoinSign(new JoinSign(arena, new Vector3d(-535, 70, 1273)));
        arena.addJoinSign(new JoinSign(arena, new Vector3d(-534, 70, 1272)));
        arena.addJoinSign(new JoinSign(arena, new Vector3d(-518, 70, 1266)));
        arena.addJoinSign(new JoinSign(arena, new Vector3d(-517, 70, 1265)));

        arena.addJoinSign(new JoinSign(arena, new Vector3d(-513, 70, 1265)));
        arena.addJoinSign(new JoinSign(arena, new Vector3d(-512, 70, 1266)));
        arena.addJoinSign(new JoinSign(arena, new Vector3d(-496, 70, 1272)));
        arena.addJoinSign(new JoinSign(arena, new Vector3d(-495, 70, 1273)));
        arena.addJoinSign(new JoinSign(arena, new Vector3d(-489, 70, 1289)));
        arena.addJoinSign(new JoinSign(arena, new Vector3d(-488, 70, 1290)));

        arena.addJoinSign(new JoinSign(arena, new Vector3d(-488, 70, 1294)));
        arena.addJoinSign(new JoinSign(arena, new Vector3d(-489, 70, 1295)));
        arena.addJoinSign(new JoinSign(arena, new Vector3d(-495, 70, 1311)));
        arena.addJoinSign(new JoinSign(arena, new Vector3d(-496, 70, 1312)));
        arena.addJoinSign(new JoinSign(arena, new Vector3d(-512, 70, 1318)));
        arena.addJoinSign(new JoinSign(arena, new Vector3d(-513, 70, 1319)));

        arena.addJoinSign(new JoinSign(arena, new Vector3d(-517, 70, 1319)));
        arena.addJoinSign(new JoinSign(arena, new Vector3d(-518, 70, 1318)));
        arena.addJoinSign(new JoinSign(arena, new Vector3d(-534, 70, 1312)));
        arena.addJoinSign(new JoinSign(arena, new Vector3d(-535, 70, 1311)));
        arena.addJoinSign(new JoinSign(arena, new Vector3d(-541, 70, 1295)));
        arena.addJoinSign(new JoinSign(arena, new Vector3d(-542, 70, 1294)));

        return arena;
    }
}
