package pl.themolka.janusz.profile;

import com.google.common.collect.ImmutableMap;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import pl.themolka.janusz.JanuszPlugin;
import pl.themolka.janusz.database.Database;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class SexCommandHandler extends JanuszPlugin.CommandHandler {
    private static final String CHANGE_PERMISSION = "janusz.command.sex.change";
    private static final Map<String, Sex> ARGUMENTS = ImmutableMap.<String, Sex>builder()
            .put("female", Sex.FEMALE)
            .put("male", Sex.MALE)
            .put("unisex", Sex.UNISEX)
            .build();

    private final JanuszPlugin plugin;
    private final Database database;

    private final ProfileDao profileDao;

    public SexCommandHandler(JanuszPlugin plugin) {
        super("sex");

        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.database = plugin.getDb();

        this.profileDao = this.database.getProfileDao();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
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

        Profile profile = localSession.getProfile();
        Sex sex = profile.getSex();

        boolean canChange = sender.hasPermission(CHANGE_PERMISSION);
        if (args.length == 0 || !canChange) {
            String changeTo = ARGUMENTS.entrySet().stream()
                    .filter(entry -> !Objects.equals(entry.getValue(), sex))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.joining("|"));

            ChatColor color = this.getSexColor(sex);
            sender.sendMessage(color + "Jesteś " + this.formatSex(sex) + color + ".");

            if (canChange) {
                sender.sendMessage(ChatColor.RED + "Użyj: /" + label + " <" + changeTo + "> by zmienić płeć");
            }
            return true;
        }

        Sex newSex = ARGUMENTS.get(args[0]);
        if (newSex == null) {
            sender.sendMessage(ChatColor.RED + "'" + args[0] + "' nie jest znajomą mi płcią. :(");
            sender.sendMessage(ChatColor.RED + "Dostępne płcie: " + String.join(", ", ARGUMENTS.keySet()));
            return true;
        } else if (Objects.equals(sex, newSex)) {
            sender.sendMessage(ChatColor.RED + "Już jesteś " + this.formatSex(sex) + ChatColor.RED + "!");
            return true;
        }

        this.database.getExecutor().submit(() -> {
            profile.setSex(newSex);
            this.profileDao.updateSex(profile);
            localSession.printSuccess("Od teraz jesteś " + this.formatSex(newSex) + LocalSession.SUCCESS + "! =)");
        });

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase(Locale.US);
            return ARGUMENTS.keySet().stream()
                    .map(String::toLowerCase)
                    .filter(arg -> arg.startsWith(input))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    private String formatSex(Sex sex) {
        ChatColor color = this.getSexColor(sex);
        switch (sex) {
            case FEMALE: return color + "dziewczyną";
            case MALE: return color + "chłopakiem";
            case UNISEX: return "unisex";
        }

        throw new IllegalArgumentException("Unknown sex");
    }

    private ChatColor getSexColor(Sex sex) {
        switch (sex) {
            case FEMALE: return ChatColor.LIGHT_PURPLE;
            case MALE: return ChatColor.BLUE;
            case UNISEX: return ChatColor.DARK_AQUA;
        }

        throw new IllegalArgumentException("Unknown sex");
    }
}
