package pl.themolka.janusz.clan;

import org.apache.commons.lang3.Validate;
import pl.themolka.janusz.chat.Chat;
import pl.themolka.janusz.profile.Session;
import pl.themolka.janusz.season.Season;
import pl.themolka.janusz.season.SeasonSupplier;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Objects;

public class ClanChat extends Chat {
    public static final String FIELD_CLAN_ID = "clan_id";

    private final Clan clan;

    public ClanChat(LocalDateTime createdAt, Season season, Clan clan, Session session,
                    String world, String text, boolean sent, int recipientCount) {
        super(createdAt, season, session, world, text, sent, recipientCount);

        this.clan = Objects.requireNonNull(clan, "clan");
    }

    public ClanChat(ResultSet resultSet, SeasonSupplier seasons, Clan clan, Session session) throws SQLException {
        super(resultSet, seasons, session);

        Objects.requireNonNull(clan, "clan");
        Validate.isTrue(clan.getId() == resultSet.getLong(FIELD_CLAN_ID), "Clan ID must match");

        this.clan = clan;
    }

    public Clan getClan() {
        return this.clan;
    }
}
