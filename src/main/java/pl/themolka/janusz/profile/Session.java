package pl.themolka.janusz.profile;

import org.apache.commons.lang3.Validate;
import pl.themolka.janusz.season.Season;
import pl.themolka.janusz.season.SeasonSupplier;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class Session {
    public static final String FIELD_ID = "id";
    public static final String FIELD_CREATED_AT = "created_at";
    public static final String FIELD_DESTROYED_AT = "destroyed_at";
    public static final String FIELD_SEASON_ID = "season_id";
    public static final String FIELD_PROFILE_ID = "profile_id";
    public static final String FIELD_USERNAME = "username";
    public static final String FIELD_USERNAME_LOWER = "username_lower";

    private long id;
    private final LocalDateTime createdAt;
    private LocalDateTime destroyedAt;
    private final Season season;
    private final Profile profile;
    private final String username;

    public Session(LocalDateTime createdAt, Season season, Profile profile, String username) {
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.season = Objects.requireNonNull(season, "season");
        this.profile = Objects.requireNonNull(profile, "profile");
        this.username = Objects.requireNonNull(username, "username");
    }

    public Session(ResultSet resultSet, SeasonSupplier seasons, Profile profile) throws SQLException {
        this(resultSet, Objects.requireNonNull(seasons, "seasons").apply(resultSet.getLong(FIELD_SEASON_ID)), profile);
    }

    public Session(ResultSet resultSet, Season season, Profile profile) throws SQLException {
        Objects.requireNonNull(resultSet, "resultSet");
        Objects.requireNonNull(season, "season");
        Objects.requireNonNull(profile, "profile");
        Validate.isTrue(season.getId() == resultSet.getLong(FIELD_SEASON_ID), "Season ID must match");
        Validate.isTrue(profile.getId() == resultSet.getLong(FIELD_PROFILE_ID), "Profile ID must match");

        this.id = resultSet.getLong(FIELD_ID);
        this.createdAt = resultSet.getTimestamp(FIELD_CREATED_AT).toLocalDateTime();
        this.season = season;
        this.profile = profile;
        this.username = resultSet.getString(FIELD_USERNAME);

        Optional.ofNullable(resultSet.getTimestamp(FIELD_DESTROYED_AT)).ifPresent(
                destroyedAt -> this.destroyedAt = destroyedAt.toLocalDateTime());
    }

    public LocalDateTime destroy(LocalDateTime when) {
        return this.destroyedAt = Objects.requireNonNull(when, "when");
    }

    public LocalDateTime destroyNow() {
        return this.destroy(LocalDateTime.now());
    }

    public long getId() {
        return this.id;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getDestroyedAt() {
        return this.destroyedAt;
    }

    public Season getSeason() {
        return this.season;
    }

    public Profile getProfile() {
        return this.profile;
    }

    public UUID getUniqueId() {
        return this.profile.getUniqueId();
    }

    public String getUsername() {
        return this.username;
    }

    public boolean isDestroyed() {
        return this.destroyedAt != null;
    }

    public void setId(long id) {
        Validate.isTrue(id >= 0, "id is negative");
        this.id = id;
    }

    public static String normalizeUsername(String username) {
        return Objects.requireNonNull(username, "username").toLowerCase(Locale.US);
    }
}
