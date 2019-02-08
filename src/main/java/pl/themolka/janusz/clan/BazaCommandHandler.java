package pl.themolka.janusz.clan;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import pl.themolka.janusz.JanuszPlugin;
import pl.themolka.janusz.profile.LocalSession;
import pl.themolka.janusz.profile.LocalSessionHandler;
import pl.themolka.janusz.util.SecureSpawn;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BazaCommandHandler extends JanuszPlugin.CommandHandler {
    private final JanuszPlugin plugin;

    private final Cache<UUID, Boolean> godCooldown = CacheBuilder.newBuilder()
            .expireAfterWrite(5L, TimeUnit.SECONDS)
            .build();

    public BazaCommandHandler(JanuszPlugin plugin) {
        super("baza");

        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ClanHandler clanHandler = this.plugin.getHandler(ClanHandler.class).orElse(null);
        if (clanHandler == null) {
            sender.sendMessage(ChatColor.RED + "Handler " + ClanHandler.class.getSimpleName() + " nie jest dostępny.");
            return true;
        }

        LocalSessionHandler localSessionHandler = this.plugin.getHandler(LocalSessionHandler.class).orElse(null);
        if (localSessionHandler == null) {
            sender.sendMessage(ChatColor.RED + "Handler " + LocalSessionHandler.class.getSimpleName() + " nie jest dostępny.");
            return true;
        }

        LocalSession localSession = localSessionHandler.getLocalSession(sender).orElse(null);
        if (localSession == null) {
            sender.sendMessage(ChatColor.RED + "Wygląda na to, że nie jesteś online :O");
            return true;
        }

        Clan clan = clanHandler.getFor(localSession.getProfile()).orElse(null);
        if (clan == null) {
            sender.sendMessage(ChatColor.RED + "Nie jesteś w żadnym klanie. Nie możesz się teleportować :(");
            return true;
        }

        Player player = localSession.getBukkit().orElseThrow(IllegalArgumentException::new);
        Location home = clan.getHomeLocation(this.plugin.getServer());

        SecureSpawn validator = new SecureSpawn(home);
        Optional<Location> secureHome = validator.resolveSecure();

        if (!secureHome.isPresent()) {
            player.sendMessage(ChatColor.RED + "Wygląda na to, że baza jest uszkodzona. Czyżby ktoś wylał w niej lawę?");
            return true;
        }

        //
        // TODO: make a delay between command execution and the actual
        //  teleportation.
        //

        this.teleport(player, secureHome.get());

        player.sendMessage(ChatColor.GREEN + "Teleportowano do bazy " + clan.getPrettyName() + ChatColor.GREEN + ". =)");
        return true;
    }

    private void teleport(Player player, Location destination) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(destination, "destination");

        player.teleport(destination, PlayerTeleportEvent.TeleportCause.COMMAND);
        this.godCooldown.put(player.getUniqueId(), true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void godCooldownAfterTeleport(EntityDamageEvent event) {
        Entity victim = event.getEntity();
        if (victim instanceof Player) {
            Player player = (Player) victim;

            if (this.godCooldown.getIfPresent(player.getUniqueId()) != null) {
                event.setCancelled(true);
            }
        }
    }
}
