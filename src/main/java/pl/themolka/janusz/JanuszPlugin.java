package pl.themolka.janusz;

import com.zaxxer.hikari.HikariConfig;
import org.bukkit.Server;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import pl.themolka.janusz.arena.ArenaHandler;
import pl.themolka.janusz.arena.sign.JoinSignHandler;
import pl.themolka.janusz.chat.ChatFormatHandler;
import pl.themolka.janusz.chat.ChatLoggerHandler;
import pl.themolka.janusz.chat.DimensionPrefixHandler;
import pl.themolka.janusz.database.Database;
import pl.themolka.janusz.death.DeathHandler;
import pl.themolka.janusz.motd.MotdHandler;
import pl.themolka.janusz.profile.LocalSessionHandler;
import pl.themolka.janusz.profile.PlayerCommandHandler;
import pl.themolka.janusz.profile.ProfileHandler;
import pl.themolka.janusz.season.SeasonHandler;
import pl.themolka.janusz.season.SeasonSupplier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class JanuszPlugin extends JavaPlugin {
    private Configuration configuration;
    private Database database;
    private Set<Handler> handlers = Collections.emptySet();

    @Override
    public void onEnable() {
        Server server = this.getServer();
        Logger logger = this.getLogger();

        this.saveDefaultConfig();
        this.configuration = new Configuration(this.getConfig());

        try {
            ConfigurationSection section = this.configuration.getDatabase();

            Properties properties = new Properties();
            for (String key : section.getKeys(false)) {
                properties.put(key, section.get(key));
            }

            this.database = new Database(new HikariConfig(properties), this.getLogger());
        } catch (Exception e) {
            this.getLogger().log(Level.SEVERE, "Could not initialize database connection", e);
            this.getServer().shutdown(); // for security reasons
        }

        this.handlers = Stream.of(
                new ArenaHandler(this),
                new ChatFormatHandler(),
                new ChatLoggerHandler(this),
                new ColoredSignsHandler(),
                new DeathHandler(this),
                new DimensionPrefixHandler(),
                new FakePlayerHandler(this),
                new FakePlayerAuthMeHandler(this),
                new GameModeFixerHandler(),
                new InstantTntHandler(this),
                new JoinSignHandler(this),
                new JoinQuitHandler(this),
                new LocalSessionHandler(this),
                new MotdHandler(this),
                new ObserverHandler(this),
                new PhantomHandler(this),
                new PlayerCommandHandler(this),
                new PlayerListHandler(),
                new ProfileHandler(this),
                new SeasonHandler(this),
                new TreeChopHandler(this),
                new VictoryHandler()
        ).collect(Collectors.toSet());

        for (Handler handler : this.handlers) {
            logger.info("Enabling '" + handler.getClass().getSimpleName() + "'...");
            try {
                handler.enable(this);
            } catch (Throwable th) {
                logger.log(Level.SEVERE, "Could not enable " + handler.getClass().getSimpleName(), th);
            }
        }
    }

    @Override
    public void onDisable() {
        Logger logger = this.getLogger();
        for (Handler handler : this.handlers) {
            logger.info("Disabling '" + handler.getClass().getSimpleName() +  "'...");
            try {
                handler.disable(this);
            } catch (Throwable th) {
                logger.log(Level.SEVERE, "Could not disable " + handler.getClass().getSimpleName(), th);
            }
        }

        if (this.database != null) {
            this.database.shutdown();
        }
    }

    public Configuration getConfiguration() {
        return this.configuration;
    }

    public Database getDb() {
        return this.database;
    }

    public <T extends Handler> Optional<T> getHandler(Class<T> clazz) {
        Objects.requireNonNull(clazz, "clazz");
        return (Optional<T>) this.handlers.stream()
                .filter(handler -> handler.getClass().equals(clazz))
                .findFirst();
    }

    public SeasonSupplier getSeasons() {
        return this.getHandler(SeasonHandler.class).orElseThrow(IllegalStateException::new);
    }

    public <E extends Event> E callEvent(E event) {
        this.getServer().getPluginManager().callEvent(Objects.requireNonNull(event, "event"));
        return event;
    }

    public void registerEvents(Listener listener) {
        this.getServer().getPluginManager().registerEvents(Objects.requireNonNull(listener, "listener"), this);
    }

    public void unregisterEvents(Listener listener) {
        HandlerList.unregisterAll(Objects.requireNonNull(listener, "listener"));
    }

    public static class Handler implements Listener {
        private final List<Listener> registeredListeners = new ArrayList<>(1);

        public void enable(JanuszPlugin plugin) {
            this.registerEvents(plugin, this);
        }

        public void disable(JanuszPlugin plugin) {
            this.unregisterAllEvents(plugin);
        }

        public void registerEvents(JanuszPlugin plugin, Listener listener) {
            Objects.requireNonNull(plugin, "plugin").registerEvents(listener);
            this.registeredListeners.add(listener);
        }

        public void unregisterEvents(JanuszPlugin plugin, Listener listener) {
            this.registeredListeners.remove(Objects.requireNonNull(listener, "listener"));
            Objects.requireNonNull(plugin, "plugin").unregisterEvents(listener);
        }

        public void unregisterAllEvents(JanuszPlugin plugin) {
            Objects.requireNonNull(plugin, "plugin");
            this.registeredListeners.forEach(plugin::unregisterEvents);
            this.registeredListeners.clear();
        }
    }
}
