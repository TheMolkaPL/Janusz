package pl.themolka.janusz.clan;

import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import pl.themolka.janusz.geometry.Vector3d;
import pl.themolka.janusz.profile.Profile;
import pl.themolka.janusz.season.Season;
import pl.themolka.janusz.season.SeasonSupplier;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class Clan {
    public static final String FIELD_ID = "id";
    public static final String FIELD_SEASON_ID = "season_id";
    public static final String FIELD_TEAM = "team";
    public static final String FIELD_TITLE = "title";
    public static final String FIELD_COLOR = "color";
    public static final String FIELD_WORLD = "world";
    public static final String FIELD_HOME_X = "home_x";
    public static final String FIELD_HOME_Y = "home_y";
    public static final String FIELD_HOME_Z = "home_z";
    public static final String FIELD_HOME_YAW = "home_yaw";

    private long id;
    private final Season season;
    private final String team;
    private final String title;
    private final ChatColor color;
    private final String world;
    private final Vector3d home;
    private final float homeYaw;

    private final Set<Long> members = new HashSet<>();

    public Clan(Season season, String team, String title, ChatColor color,
                String world, Vector3d home, float homeYaw, Set<Long> members) {
        this.validateColor(color);

        this.season = Objects.requireNonNull(season, "season");
        this.team = Objects.requireNonNull(team);
        this.title = Objects.requireNonNull(title, "title");
        this.color = color;
        this.world = Objects.requireNonNull(world, "world");
        this.home = Objects.requireNonNull(home, "home");
        this.homeYaw = homeYaw;

        this.members.addAll(Objects.requireNonNull(members, "members"));
    }

    public Clan(ResultSet resultSet, SeasonSupplier seasons, Set<Long> members) throws SQLException {
        Objects.requireNonNull(resultSet, "resultSet");
        Objects.requireNonNull(seasons, "seasons");

        this.id = resultSet.getLong(FIELD_ID);
        this.season = seasons.apply(resultSet.getLong(FIELD_SEASON_ID));
        this.team = resultSet.getString(FIELD_TEAM);
        this.title = resultSet.getString(FIELD_TITLE);

        this.color = Optional.ofNullable(parseChatColor(resultSet.getString(FIELD_COLOR)))
                .orElseThrow(IllegalArgumentException::new);

        this.world = resultSet.getString(FIELD_WORLD);
        this.home = this.parseHome(resultSet);
        this.homeYaw = resultSet.getFloat(FIELD_HOME_YAW);

        this.members.addAll(Objects.requireNonNull(members, "members"));
    }

    private void validateColor(ChatColor color) {
        Objects.requireNonNull(color, "color");
        switch (color) {
            case MAGIC:
            case BOLD:
            case STRIKETHROUGH:
            case UNDERLINE:
            case ITALIC:
            case RESET:
                throw new IllegalArgumentException("color cannot be format");
        }
    }

    private ChatColor parseChatColor(String input) {
        for (ChatColor color : ChatColor.values()) {
            if (color.getName().equalsIgnoreCase(input)) {
                this.validateColor(color);
                return color;
            }
        }

        return null;
    }

    private Vector3d parseHome(ResultSet resultSet) throws SQLException {
        return new Vector3d(resultSet.getDouble(FIELD_HOME_X),
                            resultSet.getDouble(FIELD_HOME_Y),
                            resultSet.getDouble(FIELD_HOME_Z));
    }

    public boolean contains(Profile profile) {
        return this.contains(Objects.requireNonNull(profile, "profile").getId());
    }

    public boolean contains(long profileId) {
        return this.members.contains(profileId);
    }

    public long getId() {
        return this.id;
    }

    public Season getSeason() {
        return this.season;
    }

    public String getTeam() {
        return this.team;
    }

    public String getTitle() {
        return this.title;
    }

    public ChatColor getColor() {
        return this.color;
    }

    public String getWorld() {
        return this.world;
    }

    public Vector3d getHome() {
        return this.home;
    }

    public float getHomeYaw() {
        return this.homeYaw;
    }

    public Location getHomeLocation(Server worldProvider) {
        Objects.requireNonNull(worldProvider, "worldProvider");
        return new Location(worldProvider.getWorld(this.world),
                this.home.getX(), this.home.getY(), this.home.getZ(),
                this.homeYaw, 0F);
    }

    public void setId(long id) {
        Validate.isTrue(id >= 0, "id is negative");
        this.id = id;
    }

    public Team getBukkit(ScoreboardManager scoreboardManager) {
        Objects.requireNonNull(scoreboardManager, "scoreboardManager");
        Scoreboard scoreboard = scoreboardManager.getMainScoreboard();

        Team team = scoreboard.getTeam(this.team);
        if (team == null) {
            team = scoreboard.registerNewTeam(this.team);
        }

        return team;
    }

    public void applyValues(ScoreboardManager scoreboardManager) {
        Objects.requireNonNull(scoreboardManager, "scoreboardManager");

        Team team = this.getBukkit(scoreboardManager);
        team.setDisplayName(this.title);
        team.setColor(org.bukkit.ChatColor.getByChar(this.color.toString().substring(1, 2)));
        // ^ FIXME https://github.com/SpigotMC/BungeeCord/pull/2627
        team.setPrefix(this.color.toString());
        team.setAllowFriendlyFire(true);
        team.setCanSeeFriendlyInvisibles(true);
    }

    public String getPrettyName() {
        return this.color.toString() + this.title + ChatColor.RESET;
    }
}
