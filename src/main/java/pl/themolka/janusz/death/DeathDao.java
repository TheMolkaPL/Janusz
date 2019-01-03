package pl.themolka.janusz.death;

import pl.themolka.janusz.database.Dao;
import pl.themolka.janusz.database.Database;
import pl.themolka.janusz.geometry.Vector3d;
import pl.themolka.janusz.profile.Session;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Objects;

public class DeathDao extends Dao<Death> {
    public DeathDao(Database database) {
        super(database);
    }

    public void save(Death death) {
        Objects.requireNonNull(death, "death");

        try (Connection connection = this.database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO `janusz_deaths` (`created_at`, `season_id`, `unfair`, `victim_profile_id`, `victim_session_id`," +
                    "`world`, `x`, `y`, `z`, `cause`, `fall_distance`, `killer`, `killer_profile_id`, `killer_session_id`)" +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);",
                    Statement.RETURN_GENERATED_KEYS);
            statement.setTimestamp(1, Timestamp.valueOf(death.getCreatedAt()));
            statement.setLong(2, death.getSeason().getId());
            statement.setBoolean(3, death.isUnfair());
            statement.setLong(4, death.getVictim().getProfile().getId());
            statement.setLong(5, death.getVictim().getId());
            this.attachLocation(statement, 6, death.getWorld(), death.getLocation());
            statement.setString(10, death.getCause().orElse(null));
            statement.setFloat(11, death.getFallDistance());

            Killer killer = death.getKiller().orElse(null);
            if (killer != null) {
                statement.setString(12, killer.getType());
            } else {
                statement.setNull(12, Types.VARCHAR);
            }

            if (killer instanceof PlayerKiller) {
                Session killerSession = ((PlayerKiller) killer).getSession();
                statement.setLong(13, killerSession.getProfile().getId());
                statement.setLong(14, killerSession.getId());
            } else {
                statement.setNull(13, Types.BIGINT);
                statement.setNull(14, Types.BIGINT);
            }

            statement.executeUpdate();
            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                while (resultSet.next()) {
                    death.setId(resultSet.getLong(1));
                }
            }
        } catch (SQLException e) {
            this.exceptionThrown(e);
        }
    }

    private void attachLocation(PreparedStatement statement, int indexOffset,
                                String world, Vector3d location) throws SQLException {
        statement.setString(indexOffset, world);
        statement.setDouble(indexOffset + 1, location.getX());
        statement.setDouble(indexOffset + 2, location.getY());
        statement.setDouble(indexOffset + 3, location.getZ());
    }
}
