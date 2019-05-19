/*
 * Copyright 2019 Aleksander Jagiełło <themolkapl@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.themolka.janusz.profile;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import pl.themolka.janusz.JanuszPlugin;
import pl.themolka.janusz.database.Database;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class UsernameCacheHandler extends JanuszPlugin.Handler {
    private final JanuszPlugin plugin;
    private final Database database;

    private final Map<String, Record> cache = new ConcurrentHashMap<>(256);

    private final SessionDao sessionDao;

    public UsernameCacheHandler(JanuszPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.database = plugin.getDb();

        this.sessionDao = this.database.getSessionDao();
    }

    @Override
    public void enable(JanuszPlugin plugin) {
        super.enable(plugin);

        this.database.getExecutor().submit(() -> this.plugin.getLogger()
                .log(Level.INFO, this.pullAll() + " usernames were successfully cached."));
    }

    @Override
    public void disable(JanuszPlugin plugin) {
        super.disable(plugin);
        this.flushAll();
    }

    public Set<String> findStartingWith(String startsWith) {
        Objects.requireNonNull(startsWith, "startsWith");
        String input = startsWith.toLowerCase(Locale.US);

        return this.cache.values().stream()
                .map(record -> record.username)
                .filter(username -> username.toLowerCase(Locale.US).startsWith(input))
                .collect(Collectors.toSet());
    }

    public Optional<String> findExact(String username) {
        Objects.requireNonNull(username, "username");
        String key = this.normalizeKey(username);
        return Optional.ofNullable(this.cache.get(key)).map(record -> record.username);
    }

    public void flushAll() {
        this.cache.clear();
    }

    public int pullAll() {
        Set<String> usernames = this.sessionDao.findAllUsernames();
        usernames.forEach(this::register);
        return usernames.size();
    }

    public void register(String username) {
        Objects.requireNonNull(username, "username");
        String key = this.normalizeKey(username);
        this.cache.put(key, new Record(username));
    }

    private String normalizeKey(String key) {
        return Objects.requireNonNull(key, "key").toLowerCase(Locale.US);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void registerNew(PlayerJoinEvent event) {
        this.register(event.getPlayer().getName());
    }

    class Record {
        final String username;

        Record(String username) {
            this.username = Objects.requireNonNull(username, "username");
        }
    }
}
