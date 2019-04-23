package pl.themolka.janusz.arena;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import pl.themolka.janusz.JanuszPlugin;
import pl.themolka.janusz.Message;
import pl.themolka.janusz.arena.event.MatchBeginEvent;
import pl.themolka.janusz.arena.event.MatchEndEvent;
import pl.themolka.janusz.profile.LocalSession;
import pl.themolka.janusz.profile.Sex;
import pl.themolka.janusz.season.Season;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Match extends GameState {
    public static final Message BEGIN_MESSAGE = new Message("%s" + ChatColor.DARK_AQUA +
            " rozpoczęli bitwę na arenie PVP!\n" + ChatColor.GREEN + "Jak myślicie, " +
            ChatColor.UNDERLINE + "kto wygra?" + ChatColor.RESET + ChatColor.AQUA + " ;)");
    public static final Message VICTORY_MESSAGE = new Message(ChatColor.AQUA + "%s " + ChatColor.GREEN,
            "wygrał", "wygrała", "wygrał/a", " bitwę na arenie PVP!");

    private final Set<LocalSession> competitors;
    private final Set<LocalSession> competitorsAlive;

    private boolean running;
    private Instant beganAt;

    public Match(Game game, Set<LocalSession> competitors) {
        super(game);

        Validate.isTrue(!Objects.requireNonNull(competitors).isEmpty(), "competitors.isEmpty()");
        this.competitors = new HashSet<>(competitors);
        this.competitorsAlive = new HashSet<>(game.getMinPlayerCount());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Match match = (Match) o;
        return  Objects.equals(super.game, match.game) &&
                Objects.equals(competitors, match.competitors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.game, competitors);
    }

    @Override
    public void enableState() {
        this.begin();
    }

    @Override
    public void disableState() {
        if (this.running) {
            throw new IllegalStateException("Must end(...) first");
        }
    }

    @Override
    protected boolean canJoin(LocalSession competitor) {
        competitor.printError("Gra właśnie trwa. Dołącz po jej zakończeniu.");
        return false;
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }

    @Override
    protected boolean join(LocalSession competitor) {
        return false;
    }

    @Override
    protected Optional<MatchResult> leave(LocalSession competitor) {
        return this.running ? this.die(competitor) : Optional.empty();
    }

    public Game getGame() {
        return this.game;
    }

    public JanuszPlugin getPlugin() {
        return this.plugin;
    }

    public void begin() {
        if (this.running || this.beganAt != null) {
            throw new IllegalStateException();
        }

        this.competitorsAlive.addAll(this.competitors);
        this.running = true;
        this.beganAt = Instant.now();

        String message = BEGIN_MESSAGE.unisex();
        if (this.competitorsAlive.stream().allMatch(session -> session.getProfile().getSex().equals(Sex.FEMALE))) {
            message = message.replace("rozpoczęli", "rozpoczęły");
        }

        this.plugin.getServer().broadcastMessage(String.format(message, this.competitorsAlive.stream()
                .map(LocalSession::getUsername)
                .map(username -> ChatColor.AQUA + username)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.joining(ChatColor.DARK_AQUA + " vs "))));

        this.plugin.callEvent(new MatchBeginEvent(this));

        this.getArena().getGates().forEach(Gate::open);

        this.logger.info("The match has started.");
    }

    public MatchResult end(LocalSession winner) {
        if (!this.running || this.beganAt == null) {
            throw new IllegalStateException();
        }

        Season season = this.plugin.getSeasons().current();
        Arena arena = this.getArena();

        Set<LocalSession> losers = this.competitors.stream()
                .filter(competitor -> !Objects.equals(competitor, winner))
                .collect(Collectors.toSet());

        LocalDateTime beganAtDateTime = LocalDateTime.ofInstant(this.beganAt, ZoneOffset.UTC);
        MatchResult result = new MatchResult(season, arena, winner, losers, beganAtDateTime, beganAtDateTime);

        this.plugin.callEvent(new MatchEndEvent(this, result));

        this.running = false;

        if (winner != null) {
            arena.getDefaultSpawn().spawn(winner);

            Server server = this.plugin.getServer();
            server.getScheduler().callSyncMethod(this.plugin, () -> server.broadcastMessage(
                    String.format(winner.format(VICTORY_MESSAGE), winner.getUsername())));
        }

        arena.getGates().forEach(Gate::close);

        this.logger.info("The match has ended with " + (winner != null ? "winner: " + winner : "no winner."));

        return result;
    }

    public Optional<MatchResult> die(LocalSession competitor) {
        Objects.requireNonNull(competitor, "competitor");
        if (this.competitorsAlive.remove(competitor)) {
            this.game.getArena().getDefaultSpawn().spawn(competitor);
            return this.testVictory();
        }

        return Optional.empty();
    }

    public Arena getArena() {
        return this.game.getArena();
    }

    public Set<LocalSession> getCompetitors() {
        return new HashSet<>(this.competitors);
    }

    public Set<LocalSession> getCompetitorsAlive() {
        return new HashSet<>(this.competitorsAlive);
    }

    public Instant getBeganAt() {
        return this.beganAt;
    }

    private Optional<MatchResult> testVictory() {
        if (this.competitorsAlive.isEmpty()) {
            return Optional.of(this.end(null));
        }

        List<LocalSession> asList = new ArrayList<>(this.competitorsAlive);
        if (asList.size() == 1) {
            return Optional.of(this.end(asList.get(0)));
        }

        return Optional.empty();
    }
}
