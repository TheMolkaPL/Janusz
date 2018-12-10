package pl.themolka.janusz;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_13_R2.DedicatedPlayerList;
import net.minecraft.server.v1_13_R2.DedicatedServer;
import net.minecraft.server.v1_13_R2.EnumProtocolDirection;
import net.minecraft.server.v1_13_R2.NetworkManager;
import net.minecraft.server.v1_13_R2.PacketPlayInChat;
import net.minecraft.server.v1_13_R2.PlayerConnection;
import net.minecraft.server.v1_13_R2.PlayerInteractManager;
import net.minecraft.server.v1_13_R2.WorldServer;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_13_R2.CraftServer;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.UUID;

public class JanuszPlugin extends JavaPlugin implements Listener {
    private static final InetSocketAddress LOCALHOST = new InetSocketAddress("127.0.0.1", 25565);
    private static final String PASSWORD = "3HxR7BbhZCZDqJf5";

    private static final UUID PROFILE_UUID = UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5");
    private static final String PROFILE_USERNAME = "Notch";
    private static final GameProfile PROFILE = new GameProfile(PROFILE_UUID, PROFILE_USERNAME);

    private FakePlayer player;

    @Override
    public void onEnable() {
        Server server = this.getServer();
        final DedicatedPlayerList playerList = this.resolvePlayerList(server);

        DedicatedServer dedicatedServer = playerList.getServer();
        WorldServer world = ((CraftWorld) server.getWorlds().get(0)).getHandle();
        PlayerInteractManager interactManager = this.createInteractManager(world);

        this.player = new FakePlayer(dedicatedServer, world, PROFILE, interactManager);
        NetworkManager network = new NetworkManager(EnumProtocolDirection.CLIENTBOUND) {
            @Override
            public SocketAddress getSocketAddress() {
                return LOCALHOST;
            }
        };

        this.player.playerConnection = new PlayerConnection(dedicatedServer, network, this.player);

        server.getPluginManager().registerEvents(this, this);

        this.getServer().getScheduler().runTaskLater(this, this::join, 0L);
    }

    @Override
    public void onDisable() {
        this.quit();
    }

    private DedicatedPlayerList resolvePlayerList(Server bukkit) {
        return ((CraftServer) bukkit).getHandle();
    }

    private PlayerInteractManager createInteractManager(WorldServer worldServer) {
        return new PlayerInteractManager(worldServer);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (this.playerCount() != 0) {
            this.quit();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (this.playerCount() == 0) {
            this.join();
        }
    }

    private int playerCount() {
        return this.getServer().getOnlinePlayers().size();
    }

    private void join() {
        if (this.player != null) {
            Server server = this.getServer();
            this.resolvePlayerList(server).onPlayerJoin(player, "Janusz chroni serwerek");
            this.player.playerConnection.a(new PacketPlayInChat("/login " + PASSWORD));
        }
    }

    private void quit() {
        if (this.player != null) {
            this.resolvePlayerList(this.getServer()).disconnect(this.player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTreeChop(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        try {
            if (this.isWood(block.getType()) && this.canChop(player)) {
                this.recursive(player, block);
            }
        } finally {
            this.broken = 0;
        }
    }

    private int broken = 0;

    private void recursive(Player player, Block base) {
        if (this.broken >= 50) {
            player.sendMessage(ChatColor.RED + "Za duże drzewo :(");
            return;
        }

        for (BlockFace face : BlockFace.values()) {
            Block block = base.getRelative(face);

            if (this.chop(block)) {
                this.broken++;
                this.recursive(player, block);
            }
        }
    }

    private boolean chop(Block block) {
        if (!this.isWood(block.getType())) {
            return false;
        }

        Material material = block.getType();
        block.setType(Material.AIR, true);
        block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(material, 1));
        return true;
    }

    private boolean isWood(Material material) {
        switch (material) {
            case ACACIA_WOOD:
            case BIRCH_WOOD:
            case DARK_OAK_WOOD:
            case JUNGLE_WOOD:
            case OAK_WOOD:
            case SPRUCE_WOOD:
                return true;
            default:
                return false;
        }
    }

    private boolean isTool(Material material) {
        switch (material) {
            case WOODEN_AXE:
            case STONE_AXE:
            case GOLDEN_AXE:
            case IRON_AXE:
            case DIAMOND_AXE:
                return true;
            default:
                return false;
        }
    }

    private boolean canChop(Player player) {
        if (player.isSneaking()) {
            return false;
        }

        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool == null || !this.isTool(tool.getType())) {
            return false;
        }

        switch (player.getGameMode()) {
            case SURVIVAL:
            case ADVENTURE:
                return true;
        }

        return false;
    }
}
