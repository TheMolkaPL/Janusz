package pl.themolka.janusz;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Configuration {
    private final ConfigurationSection database;
    private final long currentSeasonId;
    private final double phantomSpawnChance;
    private final List<String> observerNames;
    private final int treeSizeLimit;
    private final FakePlayer fakePlayer;

    public Configuration(FileConfiguration config) {
        this.database = config.getConfigurationSection("database");
        this.currentSeasonId = config.getLong("current-season-id");
        this.phantomSpawnChance = config.getDouble("phantom-spawn-chance");
        this.observerNames = config.getStringList("observer-names");
        this.treeSizeLimit = config.getInt("tree-size-limit");
        this.fakePlayer = new FakePlayer(config.getConfigurationSection("fake-player"));
    }

    public ConfigurationSection getDatabase() {
        return this.database;
    }

    public long getCurrentSeasonId() {
        return this.currentSeasonId;
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

    public FakePlayer getFakePlayer() {
        return this.fakePlayer;
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
