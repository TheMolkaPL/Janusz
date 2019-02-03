package pl.themolka.janusz.database;

import pl.themolka.janusz.geometry.Vector3d;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Level;

public class Dao<T> {
    protected final Database database;

    public Dao(Database database) {
        this.database = Objects.requireNonNull(database, "database");
    }

    public Database getDatabase() {
        return this.database;
    }

    protected void exceptionThrown(Exception exception) {
        this.database.getLogger().log(Level.SEVERE, "Exception thrown", exception);
    }

    protected void attachLocation(PreparedStatement statement, int indexOffset,
                                  String world, Vector3d location) throws SQLException {
        statement.setString(indexOffset, world);
        statement.setDouble(indexOffset + 1, location.getX());
        statement.setDouble(indexOffset + 2, location.getY());
        statement.setDouble(indexOffset + 3, location.getZ());
    }
}
