package pl.themolka.janusz.clan;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import pl.themolka.janusz.JanuszPlugin;
import pl.themolka.janusz.database.Database;
import pl.themolka.janusz.profile.LocalSession;
import pl.themolka.janusz.profile.LocalSessionHandler;

import java.util.Objects;

public class BazaCommandHandler extends JanuszPlugin.CommandHandler {
    private final JanuszPlugin plugin;
    private final Database database;

    public BazaCommandHandler(JanuszPlugin plugin) {
        super("baza");

        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.database = plugin.getDb();
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

        player.teleport(home, PlayerTeleportEvent.TeleportCause.COMMAND);

        player.sendMessage(ChatColor.GREEN + "Teleportowano do bazy " + clan.getPrettyName() + ChatColor.GREEN + ". =)");
        return true;
    }
}
