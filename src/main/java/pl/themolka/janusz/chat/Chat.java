package pl.themolka.janusz.chat;

import org.apache.commons.lang.Validate;
import pl.themolka.janusz.profile.Session;
import pl.themolka.janusz.season.Season;
import pl.themolka.janusz.season.SeasonSupplier;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Objects;

public class Chat {
    public static final String FIELD_ID = "id";
    public static final String FIELD_CREATED_AT = "created_at";
    public static final String FIELD_SEASON_ID = "season_id";
    public static final String FIELD_PROFILE_ID = "profile_id";
    public static final String FIELD_SESSION_ID = "session_id";
    public static final String FIELD_WORLD = "world";
    public static final String FIELD_TEXT = "text";
    public static final String FIELD_SENT = "sent";
    public static final String FIELD_RECIPIENT_COUNT = "recipient_count";

    private long id;
    private final LocalDateTime createdAt;
    private final Season season;
    private final Session session;
    private final String world;
    private final String text;
    private final boolean sent;
    private final int recipientCount;

    public Chat(LocalDateTime createdAt, Season season, Session session, String world, String text, boolean sent, int recipientCount) {
        Validate.isTrue(recipientCount >= 0, "recipientCount was negative");

        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.season = Objects.requireNonNull(season, "season");
        this.session = Objects.requireNonNull(session, "session");
        this.world = Objects.requireNonNull(world, "world");
        this.text = Objects.requireNonNull(text, "text");
        this.sent = sent;
        this.recipientCount = sent ? recipientCount : 0;
    }

    public Chat(ResultSet resultSet, SeasonSupplier seasons, Session session) throws SQLException {
        Objects.requireNonNull(resultSet, "resultSet");
        Objects.requireNonNull(seasons, "seasons");
        Objects.requireNonNull(session, "session");
        Validate.isTrue(session.getProfile().getId() == resultSet.getLong(FIELD_PROFILE_ID), "Profile ID must match");
        Validate.isTrue(session.getId() == resultSet.getLong(FIELD_SESSION_ID), "Session ID must match");

        this.id = resultSet.getLong(FIELD_ID);
        this.createdAt = resultSet.getTimestamp(FIELD_CREATED_AT).toLocalDateTime();
        this.season = seasons.apply(resultSet.getLong(FIELD_SEASON_ID));
        this.session = session;
        this.world = resultSet.getString(FIELD_WORLD);
        this.text = resultSet.getString(FIELD_TEXT);
        this.sent = resultSet.getBoolean(FIELD_SENT);
        this.recipientCount = resultSet.getInt(FIELD_RECIPIENT_COUNT);
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

    public Session getSession() {
        return this.session;
    }

    public String getWorld() {
        return this.world;
    }

    public String getText() {
        return this.text;
    }

    public boolean wasSent() {
        return this.sent;
    }

    public int getRecipientCount() {
        return this.recipientCount;
    }

    public void setId(long id) {
        Validate.isTrue(id >= 0, "id is negative");
        this.id = id;
    }
}
