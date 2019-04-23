package pl.themolka.janusz.death;

import org.apache.commons.lang.Validate;
import org.bukkit.NamespacedKey;
import pl.themolka.janusz.geometry.Vector3d;
import pl.themolka.janusz.profile.Session;
import pl.themolka.janusz.season.Season;
import pl.themolka.janusz.season.SeasonSupplier;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

public class Death {
    public static final String FIELD_ID = "id";
    public static final String FIELD_CREATED_AT = "created_at";
    public static final String FIELD_SEASON_ID = "season_id";
    public static final String FIELD_UNFAIR = "unfair";
    public static final String FIELD_VICTIM_SESSION_ID = "victim_session_id";
    public static final String FIELD_WORLD = "world";
    public static final String FIELD_X = "x";
    public static final String FIELD_Y = "y";
    public static final String FIELD_Z = "z";
    public static final String FIELD_CAUSE = "cause";
    public static final String FIELD_FALL_DISTANCE = "fall_distance";

    public static final String FIELD_KILLER = "killer";
    public static final String FIELD_KILLER_PROFILE_ID = "killer_profile_id";
    public static final String FIELD_KILLER_SESSION_ID = "killer_session_id";

    private long id;
    private final LocalDateTime createdAt;
    private final Season season;
    private final boolean unfair;
    private final Session victim;
    private final String world;
    private final Vector3d location;
    private final String cause;
    private final float fallDistance;
    private final Killer killer;

    public Death(LocalDateTime createdAt,
                 Season season,
                 boolean unfair,
                 Session victim,
                 String world,
                 Vector3d location,
                 String cause,
                 float fallDistance,
                 Killer killer) {
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.season = Objects.requireNonNull(season, "season");
        this.unfair = unfair;
        this.victim = Objects.requireNonNull(victim, "victim");
        this.world = Objects.requireNonNull(world, "world");
        this.location = Objects.requireNonNull(location, "location");
        this.cause = cause;
        this.fallDistance = fallDistance;
        this.killer = killer;
    }

    public Death(ResultSet resultSet, SeasonSupplier seasons, Session victim, Session killerSession) throws SQLException {
        Objects.requireNonNull(resultSet, "resultSet");
        Objects.requireNonNull(seasons, "seasons");
        Objects.requireNonNull(victim, "victim");
        Validate.isTrue(victim.getProfile().getId() == resultSet.getLong(FIELD_KILLER_PROFILE_ID), "Victim profile ID must match");
        Validate.isTrue(victim.getId() == resultSet.getLong(FIELD_VICTIM_SESSION_ID), "Victim session ID must match");

        if (killerSession != null) {
            Validate.isTrue(killerSession.getProfile().getId() == resultSet.getLong(FIELD_KILLER_PROFILE_ID), "Killer profile ID must match");
            Validate.isTrue(killerSession.getId() == resultSet.getLong(FIELD_KILLER_SESSION_ID), "Killer session ID must match");
        }

        this.id = resultSet.getLong(FIELD_ID);
        this.createdAt = resultSet.getTimestamp(FIELD_CREATED_AT).toLocalDateTime();
        this.season = seasons.apply(resultSet.getLong(FIELD_SEASON_ID));
        this.unfair = resultSet.getBoolean(FIELD_UNFAIR);
        this.victim = victim;
        this.world = resultSet.getString(FIELD_WORLD);
        this.location = this.parseLocation(resultSet);
        this.cause = resultSet.getString(FIELD_CAUSE);
        this.fallDistance = resultSet.getFloat(FIELD_FALL_DISTANCE);

        if (killerSession != null) {
            this.killer = new PlayerKiller(killerSession);
        } else {
            String[] keyParts = resultSet.getString(FIELD_KILLER).split("\\.", 2);
            Validate.isTrue(keyParts.length == 2, "Invalid namespaced key syntax for killer");

            this.killer = new Killer(new NamespacedKey(keyParts[0], keyParts[1]));
        }
    }

    private Vector3d parseLocation(ResultSet resultSet) throws SQLException {
        return new Vector3d(resultSet.getDouble(FIELD_X), resultSet.getDouble(FIELD_Y), resultSet.getDouble(FIELD_Z));
    }

    public long getId() {
        return this.id;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public Season getSeason() {
        return this.season;
    }

    public boolean isUnfair() {
        return this.unfair;
    }

    public Session getVictim() {
        return this.victim;
    }

    public String getWorld() {
        return this.world;
    }

    public Vector3d getLocation() {
        return this.location;
    }

    public Optional<String> getCause() {
        return Optional.ofNullable(this.cause);
    }

    public float getFallDistance() {
        return this.fallDistance;
    }

    public Optional<Killer> getKiller() {
        return Optional.ofNullable(this.killer);
    }

    public void setId(long id) {
        Validate.isTrue(id >= 0, "id is negative");
        this.id = id;
    }
}
