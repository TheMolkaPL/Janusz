package pl.themolka.janusz.profile;

import org.apache.commons.lang3.Validate;
import pl.themolka.janusz.Message;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;

public class Profile {
    public static final String FIELD_ID = "id";
    public static final String FIELD_UUID = "uuid";
    public static final String FIELD_SEX = "sex";

    private long id;
    private final UUID uniqueId;
    private final Sex sex;

    public Profile(UUID uniqueId) {
        this.uniqueId = Objects.requireNonNull(uniqueId, "uniqueId");
        this.sex = Sex.UNISEX;
    }

    public Profile(ResultSet resultSet) throws SQLException {
        Objects.requireNonNull(resultSet, "resultSet");
        this.id = resultSet.getLong(FIELD_ID);
        this.uniqueId = UUID.fromString(resultSet.getString(FIELD_UUID));
        this.sex = Sex.deserialize(resultSet.getString(FIELD_SEX));
    }

    public String format(Message message) {
        return this.sex.format(message);
    }

    public long getId() {
        return this.id;
    }

    public UUID getUniqueId() {
        return this.uniqueId;
    }

    public Sex getSex() {
        return this.sex;
    }

    public void setId(long id) {
        Validate.isTrue(id >= 0, "id is negative");
        this.id = id;
    }
}
