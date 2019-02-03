package pl.themolka.janusz.clan;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.scoreboard.ScoreboardManager;
import pl.themolka.janusz.JanuszPlugin;
import pl.themolka.janusz.database.Database;
import pl.themolka.janusz.profile.LocalSession;
import pl.themolka.janusz.profile.LocalSessionHandler;

import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public class ReloadClansCommandHandler extends JanuszPlugin.CommandHandler {
    private final JanuszPlugin plugin;
    private final Database database;

    private final ClanDao clanDao;

    public ReloadClansCommandHandler(JanuszPlugin plugin) {
        super("reloadclans");

        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.database = plugin.getDb();

        this.clanDao = this.database.getClanDao();
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

        this.database.getExecutor().submit(() -> {
            List<Clan> clans = this.clanDao.findAll(this.plugin.getSeasons());

            Server server = this.plugin.getServer();
            server.getScheduler().runTask(this.plugin, () -> {
                Logger logger = this.plugin.getLogger();
                logger.info("Got a response from the database - replacing clans and its members...");

                clanHandler.flushClans();

                ScoreboardManager scoreboardManager = server.getScoreboardManager();
                clans.forEach(clan -> {
                    clan.applyValues(scoreboardManager);
                    clanHandler.addClan(clan);
                });

                localSessionHandler.getOnline().forEach(online -> clanHandler.getFor(online.getProfile())
                        .ifPresent(clan -> clan.getBukkit(scoreboardManager).addEntry(online.getUsername())));

                logger.info("Replaced clans and its members with new values.");
                sender.sendMessage(ChatColor.GREEN + "Przeładowano klany i ich członków.");
            });
        });
        return true;
    }
}
