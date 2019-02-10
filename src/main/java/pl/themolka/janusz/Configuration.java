package pl.themolka.janusz;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Configuration {
    private final ConfigurationSection database;
    private final long currentSeasonId;
    private final boolean dimensionChatPrefix;
    private final double phantomSpawnChance;
    private final List<String> observerNames;
    private final int treeSizeLimit;
    private final boolean gameModeFixer;
    private final FakePlayer fakePlayer;
    private final int arenaMinPlayerCount;

    public Configuration(FileConfiguration config) {
        this.database = config.getConfigurationSection("database");
        this.currentSeasonId = config.getLong("current-season-id");
        this.dimensionChatPrefix = config.getBoolean("dimension-chat-prefix", false);
        this.phantomSpawnChance = config.getDouble("phantom-spawn-chance");
        this.observerNames = config.getStringList("observer-names");
        this.treeSizeLimit = config.getInt("tree-size-limit");
        this.gameModeFixer = config.getBoolean("game-mode-fixer", false);
        this.fakePlayer = new FakePlayer(config.getConfigurationSection("fake-player"));
        this.arenaMinPlayerCount = config.getInt("arena-min-player-count");
    }

    public ConfigurationSection getDatabase() {
        return this.database;
    }

    public long getCurrentSeasonId() {
        return this.currentSeasonId;
    }

    public boolean getDimensionChatPrefix() {
        return this.dimensionChatPrefix;
    }

    public double getPhantomSpawnChance() {
        return this.phantomSpawnChance;
    }

    public List<String> getObserverNames() {
        return new ArrayList<>(this.observerNames);
    }

    public int getTreeSizeLimit() {
        return this.treeSizeLimit;
    }

    public boolean getGameModeFixer() {
        return this.gameModeFixer;
    }

    public FakePlayer getFakePlayer() {
        return this.fakePlayer;
    }

    public int getArenaMinPlayerCount() {
        return this.arenaMinPlayerCount;
    }

    public static class FakePlayer {
        private final boolean enabled;
        private final UUID uuid;
        private final String username;
        private final String authMeLoginCommand;
        private final char[] authMePassword;

        public FakePlayer(ConfigurationSection config) {
            this.enabled = config.getBoolean("enabled");
            this.uuid = UUID.fromString(config.getString("uuid"));
            this.username = config.getString("username");
            this.authMeLoginCommand = config.getString("authme-login-command");
            this.authMePassword = config.getString("authme-password").toCharArray();
        }

        public boolean isEnabled() {
            return this.enabled;
        }

        public UUID getUuid() {
            return this.uuid;
        }

        public String getUsername() {
            return this.username;
        }

        public String getAuthMeLoginCommand() {
            return this.authMeLoginCommand;
        }

        public char[] getAuthMePassword() {
            return this.authMePassword;
        }
    }
}
