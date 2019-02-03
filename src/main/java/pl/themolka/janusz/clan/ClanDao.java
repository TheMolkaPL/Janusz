package pl.themolka.janusz.clan;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import pl.themolka.janusz.database.Dao;
import pl.themolka.janusz.database.Database;
import pl.themolka.janusz.season.Season;
import pl.themolka.janusz.season.SeasonSupplier;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ClanDao extends Dao<Clan> {
    public ClanDao(Database database) {
        super(database);
    }

    public List<Clan> findAll(SeasonSupplier seasons) {
        Objects.requireNonNull(seasons, "seasons");

        long currentSeasonId = seasons.current().getId();
        List<Clan> results = new ArrayList<>();
        Multimap<Long, ClanMember> allMembers = ArrayListMultimap.create(); // indexed by clan_id

        try (Connection connection = this.database.getConnection()) {
            // members
            PreparedStatement allMembersStatement = connection.prepareStatement(
                    "SELECT * FROM `janusz_clan_members` WHERE `season_id`=? LIMIT 1000;");
            allMembersStatement.setLong(1, currentSeasonId);

            ResultSet allMembersResultSet = allMembersStatement.executeQuery();
            while (allMembersResultSet.next()) {
                ClanMember member = new ClanMember(allMembersResultSet);
                allMembers.put(member.clanId, member);
            }

            // clans
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM `janusz_clans` WHERE `season_id`=? LIMIT 100;");
            statement.setLong(1, currentSeasonId);

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Set<Long> members = allMembers.get(resultSet.getLong(Clan.FIELD_ID)).stream()
                        .map(member -> member.profileId)
                        .collect(Collectors.toSet());

                results.add(new Clan(resultSet, seasons, members));
            }
        } catch (SQLException e) {
            this.exceptionThrown(e);
        }

        return results;
    }

    class ClanMember {
        static final String FIELD_ID = "id";
        static final String FIELD_CLAN_ID = "clan_id";
        static final String FIELD_PROFILE_ID = "member_profile_id";

        final long id;
        final long clanId;
        final long profileId;

        public ClanMember(ResultSet resultSet) throws SQLException {
            Objects.requireNonNull(resultSet, "resultSet");
            this.id = resultSet.getLong(FIELD_ID);
            this.clanId = resultSet.getLong(FIELD_CLAN_ID);
            this.profileId = resultSet.getLong(FIELD_PROFILE_ID);
        }
    }
}
