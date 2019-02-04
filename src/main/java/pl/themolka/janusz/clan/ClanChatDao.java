package pl.themolka.janusz.clan;

import pl.themolka.janusz.database.Dao;
import pl.themolka.janusz.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Objects;

public class ClanChatDao extends Dao<ClanChat> {
    public ClanChatDao(Database database) {
        super(database);
    }

    public void save(ClanChat chat) {
        Objects.requireNonNull(chat, "chat");

        try (Connection connection = this.database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO `janusz_clan_chats` (`created_at`, `season_id`, `clan_id`," +
                            "`profile_id`, `session_id`, `world`, `text`, `sent`, `recipient_count`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);",
                    Statement.RETURN_GENERATED_KEYS);
            statement.setTimestamp(1, Timestamp.valueOf(chat.getCreatedAt()));
            statement.setLong(2, chat.getSeason().getId());
            statement.setLong(3, chat.getClan().getId());
            statement.setLong(4, chat.getSession().getProfile().getId());
            statement.setLong(5, chat.getSession().getId());
            statement.setString(6, chat.getWorld());
            statement.setString(7, chat.getText());
            statement.setBoolean(8, chat.wasSent());
            statement.setInt(9, chat.getRecipientCount());

            statement.executeUpdate();
            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                while (resultSet.next()) {
                    chat.setId(resultSet.getLong(1));
                }
            }
        } catch (SQLException e) {
            this.exceptionThrown(e);
        }
    }
}
