package pl.themolka.janusz.motd;

import pl.themolka.janusz.database.Dao;
import pl.themolka.janusz.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MotdDao extends Dao<Motd> {
    public MotdDao(Database database) {
        super(database);
    }

    public List<Motd> findAllValid() {
        List<Motd> results = new ArrayList<>();

        try (Connection connection = this.database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM `janusz_motds` WHERE " +
                    "(`from` IS NULL OR `from` < NOW()) AND (`to` IS NULL OR `to` > NOW()) LIMIT 100;");

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                results.add(new Motd(resultSet));
            }
        } catch (SQLException e) {
            this.exceptionThrown(e);
        }

        return results;
    }
}
