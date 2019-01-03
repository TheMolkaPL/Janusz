package pl.themolka.janusz.profile;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import pl.themolka.janusz.JanuszPlugin;
import pl.themolka.janusz.Message;
import pl.themolka.janusz.database.Database;
import pl.themolka.janusz.util.PrettyDurationFormatter;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class PlayerCommandHandler extends JanuszPlugin.Handler implements CommandExecutor, TabCompleter {
    private static final String ONLINE = ChatColor.GREEN + ChatColor.BOLD.toString() + "online" + ChatColor.RESET;

    private static final Message LAST_SEEN = new Message("był ostatnio widziany",
                                                         "była ostatnio widziana",
                                                         "był/a ostatnio widziany/a");

    private final JanuszPlugin plugin;
    private final Database database;

    private final PrettyDurationFormatter formatter = new PrettyDurationFormatter();

    private final ProfileDao profileDao;
    private final SessionDao sessionDao;

    public PlayerCommandHandler(JanuszPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.database = plugin.getDb();

        this.profileDao = this.database.getProfileDao();
        this.sessionDao = this.database.getSessionDao();
    }

    @Override
    public void enable(JanuszPlugin plugin) {
        super.enable(plugin);

        PluginCommand command = plugin.getCommand("player");
        command.setExecutor(this);
        command.setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Nie podano pseudonimu gracza!");
            sender.sendMessage(ChatColor.RED + "Użycie: /" + label + " <player>");
            return true;
        }

        String query = args[0];
        if (query.length() == 36) {
            try {
                UUID uniqueId = UUID.fromString(query);
                this.database.getExecutor().submit(() -> this.findByUniqueId(sender, query, uniqueId));
            } catch (IllegalStateException e) {
                sender.sendMessage(ChatColor.RED + "Podano nieprawidłowe UUID profilu gracza.");
            }
        } else {
            this.database.getExecutor().submit(() -> this.findByUsername(sender, query));
        }

        return true;
    }

    private void findByProfile(CommandSender sender, String query, Profile profile) {
        Optional<Session> session = this.sessionDao.findLastForProfile(this.plugin.getSeasons(), profile);
        if (session.isPresent()) {
            this.printSession(sender, session.get());
        } else {
            this.printNotFound(sender, query);
        }
    }

    private void findByUniqueId(CommandSender sender, String query, UUID uniqueId) {
        Optional<Profile> profile = this.profileDao.find(uniqueId);
        if (profile.isPresent()) {
            this.findByProfile(sender, query, profile.get());
        } else {
            this.printNotFound(sender, query);
        }
    }

    private void findByUsername(CommandSender sender, String query) {
        Optional<Session> session = this.sessionDao.findLastForUsername(query,
                this.plugin.getSeasons(),
                id -> this.profileDao.find(id).orElseThrow(NullPointerException::new));
        if (session.isPresent()) {
            this.printSession(sender, session.get());
        } else {
            this.printNotFound(sender, query);
        }
    }

    private void printNotFound(CommandSender sender, String query) {
        sender.sendMessage(ChatColor.RED + "Gracz " + query + " nie został znaleziony.");
    }

    private void printSession(CommandSender sender, Session session) {
        StringBuilder builder = new StringBuilder();
        builder.append(session.isDestroyed() ? ChatColor.DARK_AQUA : ChatColor.AQUA);
        builder.append(session.getUsername()).append(ChatColor.GRAY).append(" ");

        boolean isDestroyed = session.isDestroyed();
        if (isDestroyed) {
            builder.append(session.getProfile().format(LAST_SEEN)).append(" ");
        } else {
            builder.append("jest teraz ").append(ONLINE).append(ChatColor.GRAY).append(" od ");
        }

        builder.append(ChatColor.GREEN).append(this.printSessionTime(session, isDestroyed)).append(ChatColor.GRAY);

        if (isDestroyed) {
            builder.append(" temu");
        }

        sender.sendMessage(builder.append(".").toString());
    }

    private String printSessionTime(Session session, boolean isDestroyed) {
        LocalDateTime from = isDestroyed ? session.getDestroyedAt() : session.getCreatedAt();
        Duration duration = Duration.between(from.toInstant(ZoneOffset.ofHours(1)), Instant.now());

        if (duration.getSeconds() <= 15L) {
            return isDestroyed ? "przed chwilą" : "kilku chwil";
        }

        return this.formatter.format(duration, ChatColor.GRAY + "i" + ChatColor.GREEN);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return null;
    }
}
