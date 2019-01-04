package pl.themolka.janusz.arena;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import pl.themolka.janusz.JanuszPlugin;
import pl.themolka.janusz.Message;
import pl.themolka.janusz.profile.LocalSession;
import pl.themolka.janusz.profile.LocalSessionHandler;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class QuitCommandHandler extends JanuszPlugin.CommandHandler {
    private static final Message SURRENDER = new Message(ChatColor.RED + "%s poddał", "", "a", "/a", " grę na arenie PVP.");

    private final JanuszPlugin plugin;

    private final Cache<UUID, Boolean> acceptQueue = CacheBuilder.newBuilder()
            .expireAfterWrite(15L, TimeUnit.SECONDS)
            .build();

    public QuitCommandHandler(JanuszPlugin plugin) {
        super("quit");

        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        LocalSessionHandler sessionHandler = this.plugin.getHandler(LocalSessionHandler.class).orElse(null);
        if (sessionHandler == null) {
            sender.sendMessage(ChatColor.RED + "Handler " + LocalSessionHandler.class.getSimpleName() + " nie jest dostępny.");
            return true;
        }

        LocalSession competitor = sessionHandler.getLocalSession(sender).orElse(null);
        if (competitor == null) {
            sender.sendMessage(ChatColor.RED + "Nie znaleziono twojej sesji. Jestes konsola?");
            return true;
        }

        ArenaHandler arenaHandler = this.plugin.getHandler(ArenaHandler.class).orElse(null);
        if (arenaHandler == null) {
            competitor.printError("Handler " + ArenaHandler.class.getSimpleName() + " nie jest dostępny.");
            return true;
        }

        Game game = arenaHandler.getGame();
        UUID uniqueId = competitor.getUniqueId();

        if (game.isRunning() && this.acceptQueue.getIfPresent(uniqueId) == null) {
            this.acceptQueue.put(uniqueId, true);
            competitor.printError("Potwierdź poddanie gry wpisując /" + label + " jeszcze raz.");
            return true;
        }

        this.acceptQueue.invalidate(uniqueId);
        game.leave(competitor).ifPresent(result -> {
            this.plugin.getServer().broadcastMessage(competitor.format(SURRENDER));
            game.transform(game.getFactory().idle());
        });
        return true;
    }
}
