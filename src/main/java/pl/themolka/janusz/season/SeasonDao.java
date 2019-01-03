package pl.themolka.janusz.season;

import pl.themolka.janusz.database.Dao;
import pl.themolka.janusz.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SeasonDao extends Dao<Season> {
    public SeasonDao(Database database) {
        super(database);
    }

    public List<Season> findAll() {
        List<Season> results = new ArrayList<>();

        try (Connection connection = this.database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM `janusz_seasons` LIMIT 100;");

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                results.add(new Season(resultSet));
            }
        } catch (SQLException e) {
            this.exceptionThrown(e);
        }

        return results;
    }
}
