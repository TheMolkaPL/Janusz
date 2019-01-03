package pl.themolka.janusz.arena;

import pl.themolka.janusz.database.Dao;
import pl.themolka.janusz.database.Database;
import pl.themolka.janusz.profile.LocalSession;
import pl.themolka.janusz.season.Season;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Optional;

public class MatchResultDao extends Dao<MatchResult> {
    public MatchResultDao(Database database) {
        super(database);
    }

    public void save(MatchResult result) {
        Objects.requireNonNull(result, "result");

        try (Connection connection = this.database.getConnection()) {
            this.insertResult(connection, result);

            long resultId = result.getId();
            if (resultId != 0) {
                Season season = result.getSeason();

                for (LocalSession loser : result.getLosers()) {
                    this.insertLoser(connection, season, resultId, loser.getProfile().getId(), loser.getId());
                }
            }
        } catch (SQLException e) {
            this.exceptionThrown(e);
        }
    }

    private void insertResult(Connection connection, MatchResult result) throws SQLException {
        Objects.requireNonNull(connection, "connection");
        Objects.requireNonNull(result, "result");

        Optional<LocalSession> winner = result.getWinner();
        if (!winner.isPresent()) {
            throw new IllegalStateException("result.getWinner() must be present");
        }

        PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO `janusz_match_results`(`season_id`, `arena`, `winner_profile_id`," +
                "`winner_session_id`, `began_at`, `duration`) VALUES (?, ?, ?, ?, ?, ?);",
                Statement.RETURN_GENERATED_KEYS);
        statement.setLong(1, result.getSeason().getId());
        statement.setString(2, result.getArena().getName());
        statement.setLong(3, winner.get().getProfile().getId());
        statement.setLong(4, winner.get().getId());
        statement.setTimestamp(5, Timestamp.valueOf(result.getBeganAt()));
        statement.setTime(6, Time.valueOf(LocalTime.MIDNIGHT.plus(result.getDuration())));

        statement.executeUpdate();
        try (ResultSet resultSet = statement.getGeneratedKeys()) {
            while (resultSet.next()) {
                result.setId(resultSet.getLong(1));
            }
        }
    }

    private void insertLoser(Connection connection, Season season, long resultId, long profileId, long sessionId) throws SQLException {
        Objects.requireNonNull(connection, "connection");
        Objects.requireNonNull(season, "season");

        PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO `janusz_match_result_losers` (`season_id`, `match_result_id`," +
                "`loser_profile_id`, `loser_session_id`) VALUES (?, ?, ?, ?);");
        statement.setLong(1, season.getId());
        statement.setLong(2, resultId);
        statement.setLong(3, profileId);
        statement.setLong(4, sessionId);

        statement.executeUpdate();
    }
}
