package pl.themolka.janusz.motd;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

public class Motd {
    public static final String FIELD_ID = "id";
    public static final String FIELD_FROM = "from";
    public static final String FIELD_TO = "to";
    public static final String FIELD_TEXT_PRIMARY = "text_primary";
    public static final String FIELD_TEXT_SECONDARY = "text_secondary";

    private static final char COLOR_CODE = '&';

    private long id;
    private final LocalDateTime from;
    private final LocalDateTime to;
    private final Text text;

    public Motd(LocalDateTime from, LocalDateTime to, String primary, String secondary) {
        if (from != null && to != null) {
            Validate.isTrue(from.isBefore(to), "from wasn't before to");
        }

        this.from = from;
        this.to = to;
        this.text = new Text(primary, secondary);
    }

    public Motd(ResultSet resultSet) throws SQLException {
        Objects.requireNonNull(resultSet, "resultSet");
        this.id = resultSet.getLong(FIELD_ID);
        this.from = Optional.ofNullable(resultSet.getTimestamp(FIELD_FROM)).map(Timestamp::toLocalDateTime).orElse(null);
        this.to = Optional.ofNullable(resultSet.getTimestamp(FIELD_TO)).map(Timestamp::toLocalDateTime).orElse(null);
        this.text = new Text(resultSet.getString(FIELD_TEXT_PRIMARY), resultSet.getString(FIELD_TEXT_SECONDARY));
    }

    public long getId() {
        return this.id;
    }

    public String getText() {
        return this.text.toString();
    }

    public void setId(long id) {
        Validate.isTrue(id >= 0, "id is negative");
        this.id = id;
    }

    public boolean isValid(LocalDateTime when) {
        when = Optional.ofNullable(when).orElseGet(LocalDateTime::now);
        return  (this.from == null || this.from.isBefore(when)) &&
                (this.to == null || this.to.isAfter(when));
    }

    public boolean isValidNow() {
        return this.isValid(null);
    }

    class Text {
        static final char UNIX_NEW_LINE = '\n';

        final String primary;
        final String secondary;

        Text(String primary, String secondary) {
            this.primary = this.color(Objects.requireNonNull(primary, "primary"));
            this.secondary = this.color(Optional.ofNullable(secondary).orElse(""));
        }

        @Override
        public String toString() {
            return this.primary + UNIX_NEW_LINE + this.secondary;
        }

        String color(String text) {
            return ChatColor.translateAlternateColorCodes(COLOR_CODE, text);
        }
    }
}
