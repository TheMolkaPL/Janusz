package pl.themolka.janusz.chat;

import pl.themolka.janusz.database.Dao;
import pl.themolka.janusz.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Objects;

public class ChatDao extends Dao<Chat> {
    public ChatDao(Database database) {
        super(database);
    }

    public void save(Chat chat) {
        Objects.requireNonNull(chat, "chat");

        try (Connection connection = this.database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO `janusz_chats` (`created_at`, `season_id`," +
                    "`profile_id`, `session_id`, `world`, `text`, `sent`, `recipient_count`) VALUES (?, ?, ?, ?, ?, ?, ?, ?);",
                    Statement.RETURN_GENERATED_KEYS);
            statement.setTimestamp(1, Timestamp.valueOf(chat.getCreatedAt()));
            statement.setLong(2, chat.getSeason().getId());
            statement.setLong(3, chat.getSession().getProfile().getId());
            statement.setLong(4, chat.getSession().getId());
            statement.setString(5, chat.getWorld());
            statement.setString(6, chat.getText());
            statement.setBoolean(7, chat.wasSent());
            statement.setInt(8, chat.getRecipientCount());

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
