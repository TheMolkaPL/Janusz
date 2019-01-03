package pl.themolka.janusz.profile;

import pl.themolka.janusz.database.Dao;
import pl.themolka.janusz.database.Database;
import pl.themolka.janusz.season.SeasonSupplier;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class SessionDao extends Dao<Session> {
    public SessionDao(Database database) {
        super(database);
    }

    public Optional<Session> findLastForProfile(SeasonSupplier seasons, Profile profile) {
        Objects.requireNonNull(seasons, "seasons");
        Objects.requireNonNull(profile, "profile");

        try (Connection connection = this.database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM `janusz_sessions` WHERE `profile_id`=? ORDER BY `id` DESC LIMIT 1;");
            statement.setLong(1, profile.getId());

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                return Optional.of(new Session(resultSet, seasons, profile));
            }
        } catch (SQLException e) {
            this.exceptionThrown(e);
        }

        return Optional.empty();
    }

    public Optional<Session> findLastForUsername(String username, SeasonSupplier seasons,
                                                 Function<Long, Profile> profileSupplier) {
        Objects.requireNonNull(username, "username");
        Objects.requireNonNull(seasons, "seasons");
        Objects.requireNonNull(profileSupplier, "profileSupplier");

        try (Connection connection = this.database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM `janusz_sessions` WHERE `username_lower`=? ORDER BY `id` DESC LIMIT 1;");
            statement.setString(1, Session.normalizeUsername(username));

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                return Optional.of(new Session(resultSet, seasons,
                        profileSupplier.apply(resultSet.getLong(Session.FIELD_PROFILE_ID))));
            }
        } catch (SQLException e) {
            this.exceptionThrown(e);
        }

        return Optional.empty();
    }

    public void destroy(long id, LocalDateTime when) {
        when = Optional.ofNullable(when).orElseGet(LocalDateTime::now);

        try (Connection connection = this.database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                    "UPDATE `janusz_sessions` SET `destroyed_at`=? WHERE id=?;");
            statement.setTimestamp(1, Timestamp.valueOf(when));
            statement.setLong(2, id);

            statement.executeUpdate();
        } catch (SQLException e) {
            this.exceptionThrown(e);
        }
    }

    public void destroyAll(LocalDateTime when) {
        when = Optional.ofNullable(when).orElseGet(LocalDateTime::now);

        try (Connection connection = this.database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                    "UPDATE `janusz_sessions` SET `destroyed_at`=? WHERE `destroyed_at` IS NULL;");
            statement.setTimestamp(1, Timestamp.valueOf(when));

            statement.executeUpdate();
        } catch (SQLException e) {
            this.exceptionThrown(e);
        }
    }

    public void destroyAllForProfile(long profileId, LocalDateTime when) {
        when = Optional.ofNullable(when).orElseGet(LocalDateTime::now);

        try (Connection connection = this.database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                    "UPDATE `janusz_sessions` SET `destroyed_at`=? WHERE `profile_id`=? AND `destroyed_at` IS NULL;");
            statement.setTimestamp(1, Timestamp.valueOf(when));
            statement.setLong(2, profileId);

            statement.executeUpdate();
        } catch (SQLException e) {
            this.exceptionThrown(e);
        }
    }

    public void save(Session session) {
        Objects.requireNonNull(session, "session");

        try (Connection connection = this.database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO `janusz_sessions` (`created_at`, `season_id`, `profile_id`," +
                    "`username`, `username_lower`) VALUES (?, ?, ?, ?, ?);",
                    Statement.RETURN_GENERATED_KEYS);
            statement.setTimestamp(1, Timestamp.valueOf(session.getCreatedAt()));
            statement.setLong(2, session.getSeason().getId());
            statement.setLong(3, session.getProfile().getId());
            statement.setString(4, session.getUsername());
            statement.setString(5, Session.normalizeUsername(session.getUsername()));

            statement.executeUpdate();
            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                while (resultSet.next()) {
                    session.setId(resultSet.getLong(1));
                }
            }
        } catch (SQLException e) {
            this.exceptionThrown(e);
        }
    }
}
