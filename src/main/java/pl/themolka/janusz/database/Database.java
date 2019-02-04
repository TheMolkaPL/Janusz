package pl.themolka.janusz.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import pl.themolka.janusz.arena.MatchResult;
import pl.themolka.janusz.arena.MatchResultDao;
import pl.themolka.janusz.chat.ChatDao;
import pl.themolka.janusz.clan.ClanChatDao;
import pl.themolka.janusz.clan.ClanDao;
import pl.themolka.janusz.death.DeathDao;
import pl.themolka.janusz.motd.MotdDao;
import pl.themolka.janusz.profile.ProfileDao;
import pl.themolka.janusz.profile.SessionDao;
import pl.themolka.janusz.season.SeasonDao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Database {
    private final ExecutorService executorService;
    private final HikariDataSource dataSource;
    private final Logger logger;

    public Database(HikariConfig config, Logger logger) {
        this.executorService = Executors.newSingleThreadExecutor();
        this.dataSource = new HikariDataSource(Objects.requireNonNull(config, "config"));
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    public ExecutorService getExecutor() {
        return this.executorService;
    }

    public Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }

    public HikariDataSource getDataSource() {
        return this.dataSource;
    }

    public Logger getLogger() {
        return this.logger;
    }

    public void shutdown() {
        try {
            this.dataSource.close();
        } catch (Exception e) {
            this.logger.log(Level.SEVERE, "Could not shutdown the database", e);
        } finally {
            try {
                this.executorService.shutdown();
                this.executorService.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                this.logger.log(Level.SEVERE, "Could not shutdown executor for the database", e);
            }
        }
    }

    //
    // DAOs
    //

    private final ChatDao chatDao = new ChatDao(this);
    private final ClanDao clanDao = new ClanDao(this);
    private final ClanChatDao clanChatDao = new ClanChatDao(this);
    private final DeathDao deathDao = new DeathDao(this);
    private final MatchResultDao matchResultDao = new MatchResultDao(this);
    private final MotdDao motdDao = new MotdDao(this);
    private final ProfileDao profileDao = new ProfileDao(this);
    private final SeasonDao seasonDao = new SeasonDao(this);
    private final SessionDao sessionDao = new SessionDao(this);

    public ChatDao getChatDao() {
        return this.chatDao;
    }

    public ClanDao getClanDao() {
        return this.clanDao;
    }

    public ClanChatDao getClanChatDao() {
        return this.clanChatDao;
    }

    public DeathDao getDeathDao() {
        return this.deathDao;
    }

    public MatchResultDao getMatchResultDao() {
        return this.matchResultDao;
    }

    public MotdDao getMotdDao() {
        return this.motdDao;
    }

    public ProfileDao getProfileDao() {
        return this.profileDao;
    }

    public SeasonDao getSeasonDao() {
        return this.seasonDao;
    }

    public SessionDao getSessionDao() {
        return this.sessionDao;
    }
}
