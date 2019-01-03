package pl.themolka.janusz.database;

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
}
