package pl.themolka.janusz.season;

import org.apache.commons.lang3.Validate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

public class Season {
    public static final String FIELD_ID = "id";
    public static final String FIELD_FROM = "from";
    public static final String FIELD_TO = "to";

    private long id;
    private final LocalDateTime from;
    private final LocalDateTime to;

    public Season(LocalDateTime from, LocalDateTime to) {
        this.from = Objects.requireNonNull(from, "from");
        this.to = to;
    }

    public Season(ResultSet resultSet) throws SQLException {
        Objects.requireNonNull(resultSet, "resultSet");
        this.id = resultSet.getLong(FIELD_ID);
        this.from = resultSet.getTimestamp(FIELD_FROM).toLocalDateTime();
        this.to = Optional.ofNullable(resultSet.getTimestamp(FIELD_TO)).map(Timestamp::toLocalDateTime).orElse(null);
    }

    public long getId() {
        return this.id;
    }

    public LocalDateTime getFrom() {
        return this.from;
    }

    public LocalDateTime getTo() {
        return this.to;
    }

    public void setId(long id) {
        Validate.isTrue(id >= 0, "id is negative");
        this.id = id;
    }
}
