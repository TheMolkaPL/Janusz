package pl.themolka.janusz.profile;

import pl.themolka.janusz.database.Dao;
import pl.themolka.janusz.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class ProfileDao extends Dao<Profile> {
    public ProfileDao(Database database) {
        super(database);
    }

    public Optional<Profile> find(long id) {
        try (Connection connection = this.database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM `janusz_profiles` WHERE `id`=? LIMIT 1;");
            statement.setLong(1, id);

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                return Optional.of(new Profile(resultSet));
            }
        } catch (SQLException e) {
            this.exceptionThrown(e);
        }

        return Optional.empty();
    }

    public Optional<Profile> find(UUID uniqueId, boolean online, boolean offline) {
        Objects.requireNonNull(uniqueId, "uniqueId");

        FindQuery query;
        if (online && offline) {
            query = new FindQuery() {
                @Override
                public String where() {
                    return "`uuid`=? OR `offline_uuid`=?";
                }

                @Override
                public void prepare(PreparedStatement statement) throws SQLException {
                    String string = uniqueId.toString();
                    statement.setString(1, string);
                    statement.setString(2, string);
                }
            };
        } else if (online) {
            query = new FindQuery() {
                @Override
                public String where() {
                    return "`uuid`=?;";
                }

                @Override
                public void prepare(PreparedStatement statement) throws SQLException {
                    statement.setString(1, uniqueId.toString());
                }
            };
        } else if (offline) {
            query = new FindQuery() {
                @Override
                public String where() {
                    return "`offline_uuid`=?;";
                }

                @Override
                public void prepare(PreparedStatement statement) throws SQLException {
                    statement.setString(1, uniqueId.toString());
                }
            };
        } else {
            throw new IllegalStateException("online or/and offline must be defined to true.");
        }

        try (Connection connection = this.database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM `janusz_profiles` WHERE " + query.where() + " LIMIT 1;");
            query.prepare(statement);

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                return Optional.of(new Profile(resultSet));
            }
        } catch (SQLException e) {
            this.exceptionThrown(e);
        }

        return Optional.empty();
    }

    public void save(Profile profile) throws SQLException {
        Objects.requireNonNull(profile, "profile");

        try (Connection connection = this.database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO `janusz_profiles` (`uuid`, `offline_uuid`) VALUES (?, ?);",
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, profile.getUniqueId().toString());
            statement.setString(2, profile.getOfflineId().toString());

            statement.executeUpdate();
            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                while (resultSet.next()) {
                    profile.setId(resultSet.getLong(1));
                }
            }
        }
    }

    interface FindQuery {
        String where();

        void prepare(PreparedStatement statement) throws SQLException;
    }
}
