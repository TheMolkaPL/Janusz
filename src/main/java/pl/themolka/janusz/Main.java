package pl.themolka.janusz;

import com.zaxxer.hikari.HikariConfig;
import pl.themolka.janusz.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getSimpleName());

    public static void main(String[] args) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost/janusz_season_2");
        config.setUsername("root");
        config.setMaximumPoolSize(1);

        Database database = new Database(config, LOGGER);

        long seasonId = 2;

        Main main = new Main(database);
        Map<Long, Result> times = main.calculate(seasonId);

        System.out.println();
        System.out.println("Czas gry (sezon " + seasonId + "):");
        times.forEach((key, result) -> System.out.println(key + " - " + result.username + ": " + format(result.timePlayed)));

        System.out.println();
        System.out.println("Wejść na serwer (sezon " + seasonId + "):");
        times.forEach((key, result) -> System.out.println(key + " - " + result.username + ": " + result.joins + "x"));
    }

    private static String format(long time) {
        return TimeUnit.MILLISECONDS.toHours(time) + "h";
    }

    private final Database database;

    public Main(Database database) {
        this.database = Objects.requireNonNull(database, "database");
    }

    public Map<Long, Result> calculate(long seasonId) {
        Map<Long, Result> times = new TreeMap<>(Comparator.naturalOrder());

        try (Connection connection = this.database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM `janusz_sessions` WHERE `season_id`=?;");
            statement.setLong(1, seasonId);

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Timestamp createdAt = resultSet.getTimestamp("created_at");
                Timestamp destroyedAt = resultSet.getTimestamp("destroyed_at");
                long profileId = resultSet.getLong("profile_id");
                String username = resultSet.getString("username");

                if (destroyedAt == null) {
                    continue;
                }

                long duration = destroyedAt.getTime() - createdAt.getTime();
                if (duration <= 0) {
                    continue;
                }

                Result result = times.computeIfAbsent(profileId, id -> new Result());
                result.username = username;
                result.timePlayed += duration;
                result.joins++;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Could not handle SQL query", e);
        }

        return times;
    }

    class Result {
        String username;
        long timePlayed;
        int joins;
    }
}
