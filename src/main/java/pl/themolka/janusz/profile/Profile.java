package pl.themolka.janusz.profile;

import org.apache.commons.lang.Validate;
import pl.themolka.janusz.Message;

import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;

public class Profile {
    public static final String FIELD_ID = "id";
    public static final String FIELD_UUID = "uuid";
    public static final String FIELD_OFFLINE_UUID = "offline_uuid";
    public static final String FIELD_SEX = "sex";

    private long id;
    private final UUID uniqueId;
    private final UUID offlineId;
    private Sex sex = Sex.UNISEX;

    public Profile(UUID uniqueId, UUID offlineId) {
        this.uniqueId = Objects.requireNonNull(uniqueId, "uniqueId");
        this.offlineId = Objects.requireNonNull(offlineId, "offlineId");
    }

    public Profile(ResultSet resultSet) throws SQLException {
        Objects.requireNonNull(resultSet, "resultSet");
        this.id = resultSet.getLong(FIELD_ID);
        this.uniqueId = UUID.fromString(resultSet.getString(FIELD_UUID));
        this.offlineId = UUID.fromString(resultSet.getString(FIELD_OFFLINE_UUID));
        this.sex = Sex.deserialize(resultSet.getString(FIELD_SEX));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Profile profile = (Profile) o;
        return id == profile.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
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

    public UUID getOfflineId() {
        return this.offlineId;
    }

    public Sex getSex() {
        return this.sex;
    }

    public void setId(long id) {
        Validate.isTrue(id >= 0, "id is negative");
        this.id = id;
    }

    public void setSex(Sex sex) {
        this.sex = Objects.requireNonNull(sex, "sex");
    }

    public static UUID getOfflineId(String username) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8));
    }
}
