package pl.themolka.janusz.arena;

import org.apache.commons.lang.Validate;
import pl.themolka.janusz.profile.LocalSession;
import pl.themolka.janusz.season.Season;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class MatchResult {
    private long id;
    private final Season season;
    private final Arena arena;
    private final LocalSession winner;
    private final Set<LocalSession> losers;
    private final LocalDateTime beganAt;
    private final Duration duration;

    public MatchResult(Season season,
                       Arena arena,
                       LocalSession winner,
                       Set<LocalSession> losers,
                       LocalDateTime beganAt,
                       LocalDateTime endedAt) {
        this.season = Objects.requireNonNull(season, "season");
        this.arena = Objects.requireNonNull(arena, "arena");
        this.winner = winner;
        this.losers = Objects.requireNonNull(losers, "losers");
        this.beganAt = Objects.requireNonNull(beganAt, "beganAt");
        this.duration = Duration.between(this.beganAt, Objects.requireNonNull(endedAt, "endedAt"));
    }

    public long getId() {
        return this.id;
    }

    public Season getSeason() {
        return this.season;
    }

    public Arena getArena() {
        return this.arena;
    }

    public Optional<LocalSession> getWinner() {
        return Optional.ofNullable(this.winner);
    }

    public Set<LocalSession> getLosers() {
        return new HashSet<>(this.losers);
    }

    public LocalDateTime getBeganAt() {
        return this.beganAt;
    }

    public Duration getDuration() {
        return this.duration;
    }

    public void setId(long id) {
        Validate.isTrue(id >= 0, "id is negative");
        this.id = id;
    }
}
