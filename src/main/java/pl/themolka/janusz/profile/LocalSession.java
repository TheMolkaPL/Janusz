package pl.themolka.janusz.profile;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import pl.themolka.janusz.JanuszPlugin;
import pl.themolka.janusz.Message;
import pl.themolka.janusz.season.Season;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

public class LocalSession extends Session {
    public static final ChatColor ERROR = ChatColor.RED;
    public static final ChatColor SUCCESS = ChatColor.GREEN;

    protected final JanuszPlugin plugin;

    private final Reference<Player> bukkit;

    public LocalSession(JanuszPlugin plugin, LocalDateTime createdAt, Season season, Profile profile, Player bukkit) {
        super(createdAt, season, profile, Objects.requireNonNull(bukkit, "bukkit").getName());
        this.plugin = Objects.requireNonNull(plugin, "plugin");

        this.bukkit = new WeakReference<>(bukkit);
    }

    public JanuszPlugin getPlugin() {
        return this.plugin;
    }

    public Server getServer() {
        return this.plugin.getServer();
    }

    public Optional<Player> getBukkit() {
        return Optional.ofNullable(this.bukkit.get());
    }

    public boolean isOnline() {
        return this.getBukkit().isPresent();
    }

    public String format(Message message) {
        return this.getProfile().format(message);
    }

    public void print(Message message) {
        this.print(this.format(Objects.requireNonNull(message, "message")));
    }

    public void print(String message) {
        Objects.requireNonNull(message, "message");
        this.getBukkit().ifPresent(bukkit -> bukkit.sendMessage(message));
    }

    public void printError(Message error) {
        this.printError(this.format(Objects.requireNonNull(error, "error")));
    }

    public void printError(String error) {
        this.print(ERROR + Objects.requireNonNull(error, "error"));
    }

    public void printSuccess(Message success) {
        this.printSuccess(this.format(Objects.requireNonNull(success, "success")));
    }

    public void printSuccess(String success) {
        this.print(SUCCESS + Objects.requireNonNull(success, "success"));
    }
}
