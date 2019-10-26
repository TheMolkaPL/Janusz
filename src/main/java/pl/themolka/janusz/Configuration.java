package pl.themolka.janusz;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class Configuration {
    private final ConfigurationSection database;
    private final long currentSeasonId;
    private final List<String> observerNames;

    public Configuration(FileConfiguration config) {
        this.database = config.getConfigurationSection("database");
        this.currentSeasonId = config.getLong("current-season-id");
        this.observerNames = config.getStringList("observer-names");
    }

    public ConfigurationSection getDatabase() {
        return this.database;
    }

    public long getCurrentSeasonId() {
        return this.currentSeasonId;
    }

    public List<String> getObserverNames() {
        return new ArrayList<>(this.observerNames);
    }
}
