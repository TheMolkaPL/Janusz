package pl.themolka.janusz.arena;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;
import pl.themolka.janusz.Message;
import pl.themolka.janusz.profile.LocalSession;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public class Idle extends GameState.Queue {
    public static final BaseComponent[] EMPTY = TextComponent.fromLegacyText("");
    public static final BaseComponent[] INFO = TextComponent.fromLegacyText(ChatColor.RED +
            "Jesteś w kolejce do gry na arenie PVP. " + ChatColor.GREEN + "Użyj " +
            ChatColor.DARK_AQUA + "/quit" + ChatColor.GREEN + ", by wyjść z kolejki.");

    private static final Message LEAVE = new Message("Opuścił", "eś", "aś", "eś/aś", " kolejkę.");

    private static final Consumer<LocalSession> INFORM = competitor -> competitor.getBukkit()
            .ifPresent(player -> player.spigot().sendMessage(ChatMessageType.ACTION_BAR, INFO));
    private static final Consumer<LocalSession> REMOVE_MESSAGE = competitor -> competitor.getBukkit()
            .ifPresent(player -> player.spigot().sendMessage(ChatMessageType.ACTION_BAR, EMPTY));

    private BukkitRunnable informer;

    public Idle(Game game) {
        this(game, null);
    }

    public Idle(Game game, Set<LocalSession> queue) {
        super(game, queue);
    }

    @Override
    public void enableState() {
        Spawn defaultSpawn = this.game.getArena().getDefaultSpawn();
        this.queue.forEach(defaultSpawn::spawn);

        this.informer = new BukkitRunnable() {
            @Override
            public void run() {
                queue.forEach(INFORM);
            }
        };
        this.informer.runTaskTimer(this.plugin, 0L, 1L);
    }

    @Override
    public void disableState() {
        if (this.informer != null) {
            this.informer.cancel();
        }

        this.queue.forEach(INFORM);
    }

    @Override
    protected Optional<MatchResult> leave(LocalSession competitor) {
        REMOVE_MESSAGE.accept(competitor);

        if (this.queue.contains(competitor)) {
            competitor.printError(LEAVE);
        }

        return super.leave(competitor);
    }

    @Override
    public void testForNewState() {
        if (this.canStart()) {
            this.game.transform(this.game.getFactory().starting(this.queue));
        }
    }
}
