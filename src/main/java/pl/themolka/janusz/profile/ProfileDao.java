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

    public Optional<Profile> find(UUID uniqueId) {
        Objects.requireNonNull(uniqueId, "uniqueId");

        try (Connection connection = this.database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM `janusz_profiles` WHERE `uuid`=? LIMIT 1;");
            statement.setString(1, uniqueId.toString());

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
                    "INSERT INTO `janusz_profiles` (`uuid`) VALUES (?);",
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, profile.getUniqueId().toString());

            statement.executeUpdate();
            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                while (resultSet.next()) {
                    profile.setId(resultSet.getLong(1));
                }
            }
        }
    }
}
