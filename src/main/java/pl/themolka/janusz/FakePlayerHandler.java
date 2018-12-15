package pl.themolka.janusz;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_13_R2.DedicatedPlayerList;
import net.minecraft.server.v1_13_R2.DedicatedServer;
import net.minecraft.server.v1_13_R2.EntityPlayer;
import net.minecraft.server.v1_13_R2.EnumProtocolDirection;
import net.minecraft.server.v1_13_R2.MinecraftServer;
import net.minecraft.server.v1_13_R2.NetworkManager;
import net.minecraft.server.v1_13_R2.PlayerConnection;
import net.minecraft.server.v1_13_R2.PlayerInteractManager;
import net.minecraft.server.v1_13_R2.WorldServer;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_13_R2.CraftServer;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FakePlayerHandler extends JanuszPlugin.Handler {
    private static final InetSocketAddress ADDRESS = new InetSocketAddress("127.0.0.1", 25565);

    public static final GameProfile PROFILE = new GameProfile(
            UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5"),
            "Notch");

    private final Server server;
    private final Logger logger;

    private FakePlayerSession session;

    public FakePlayerHandler(Server server, Logger logger) {
        this.server = Objects.requireNonNull(server, "server");
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    @Override
    public void enable(JanuszPlugin plugin) {
        super.enable(plugin);

        if (this.getTotalPlayerCount() <= 0) {
            this.server.getScheduler().runTaskLater(plugin, this::createSession, 0L);
        }
    }

    @Override
    public void disable(JanuszPlugin plugin) {
        if (this.session != null) {
            this.destroySession();
        }

        super.disable(plugin);
    }

    public int getTotalPlayerCount() {
        return (int) this.server.getOnlinePlayers().stream()
                .filter(player -> !player.getUniqueId().equals(PROFILE.getId()))
                .count();
    }

    public DedicatedPlayerList resolvePlayerList(Server bukkit) {
        return ((CraftServer) Objects.requireNonNull(bukkit, "bukkit")).getHandle();
    }

    private FakePlayer createDefaultFakePlayer() {
        WorldServer world = ((CraftWorld) this.server.getWorlds().get(0)).getHandle();
        return this.createFakePlayer(
                this.resolvePlayerList(this.server),
                world,
                this.createInteractManager(world),
                this.createNetworkManager());
    }

    private FakePlayer createFakePlayer(DedicatedPlayerList playerList,
                                        WorldServer world,
                                        PlayerInteractManager interactManager,
                                        NetworkManager networkManager) {
        DedicatedServer server = Objects.requireNonNull(playerList, "playerList").getServer();

        FakePlayer player = new FakePlayer(
                server,
                Objects.requireNonNull(world, "world"),
                PROFILE,
                Objects.requireNonNull(interactManager, "interactManager"));
        player.playerConnection = new PlayerConnection(
                server,
                Objects.requireNonNull(networkManager, "networkManager"),
                player);
        return player;
    }

    private PlayerInteractManager createInteractManager(WorldServer world) {
        return new PlayerInteractManager(Objects.requireNonNull(world, "world"));
    }

    private NetworkManager createNetworkManager() {
        return new NetworkManager(EnumProtocolDirection.CLIENTBOUND) {
            @Override
            public SocketAddress getSocketAddress() {
                return ADDRESS;
            }
        };
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void testServer(PlayerJoinEvent event) {
        if (this.getTotalPlayerCount() != 0 && this.session != null) {
            this.destroySession();
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void testServer(PlayerQuitEvent event) {
        if (this.getTotalPlayerCount() <= 1 && this.session == null) {
            this.createSession();
        }
    }

    private void createSession() {
        DedicatedPlayerList playerList = this.resolvePlayerList(this.server);
        FakePlayer fakePlayer = this.createDefaultFakePlayer();

        this.logger.log(Level.INFO, "Adding fake player to the server...");

        try {
            playerList.onPlayerJoin(fakePlayer, null);
        } finally {
            this.session = new FakePlayerSession(fakePlayer);

            this.server.getPluginManager().callEvent(new FakePlayerRegisterEvent(this, this.session));
        }
    }

    private void destroySession() {
        DedicatedPlayerList playerList = this.resolvePlayerList(this.server);
        FakePlayer fakePlayer = this.session.player;

        if (playerList.players.contains(fakePlayer)) {
            this.logger.log(Level.INFO, "Removing fake player from the server...");

            try {
                playerList.disconnect(fakePlayer);
            } finally {
                this.session = null;
            }
        }
    }

    public static class FakePlayerSession {
        private final FakePlayer player;

        public FakePlayerSession(FakePlayer player) {
            this.player = Objects.requireNonNull(player, "player");
        }

        public FakePlayer getPlayer() {
            return this.player;
        }
    }

    public static class FakePlayer extends EntityPlayer {
        public FakePlayer(MinecraftServer minecraftserver,
                          WorldServer worldserver,
                          GameProfile gameprofile,
                          PlayerInteractManager playerinteractmanager) {
            super(minecraftserver, worldserver, gameprofile, playerinteractmanager);
        }

        @Override
        public void tick() {
        }

        @Override
        public void playerTick() {
        }
    }

    public static class FakePlayerRegisterEvent extends Event {
        private static final HandlerList handlerList = new HandlerList();

        private FakePlayerHandler handler;
        private FakePlayerSession session;

        public FakePlayerRegisterEvent(FakePlayerHandler handler, FakePlayerSession session) {
            this.handler = Objects.requireNonNull(handler, "handler");
            this.session = Objects.requireNonNull(session, "session");
        }

        public FakePlayerHandler getHandler() {
            return this.handler;
        }

        public FakePlayerSession getSession() {
            return this.session;
        }

        @Override
        public HandlerList getHandlers() {
            return handlerList;
        }

        public static HandlerList getHandlerList() {
            return handlerList;
        }
    }

    //
    // Cosmetics
    //

    @EventHandler(priority = EventPriority.HIGHEST)
    public void removeJoinMessage(PlayerJoinEvent event) {
        if (event.getPlayer().getUniqueId().equals(PROFILE.getId())) {
            event.setJoinMessage(null);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void removeQuitMMessage(PlayerQuitEvent event) {
        if (event.getPlayer().getUniqueId().equals(PROFILE.getId())) {
            event.setQuitMessage(null);
        }
    }
}
