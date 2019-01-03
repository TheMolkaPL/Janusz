package pl.themolka.janusz.arena;

import org.apache.commons.lang3.Validate;
import pl.themolka.janusz.profile.LocalSession;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class DedicatedSpawnLibrary {
    private final LinkedList<Spawn> spawns;
    private final Map<UUID, DedicatedSpawn> dedicated = new HashMap<>();

    public DedicatedSpawnLibrary(Set<Spawn> spawns) {
        this.spawns = new LinkedList<>(Objects.requireNonNull(spawns, "spawns"));
        Validate.isTrue(!this.spawns.isEmpty(), "spawns.isEmpty()");
    }

    public boolean isResolved(UUID ownerId) {
        return this.dedicated.containsKey(Objects.requireNonNull(ownerId, "ownerId"));
    }

    public boolean isResolved(LocalSession owner) {
        return this.isResolved(Objects.requireNonNull(owner, "owner").getUniqueId());
    }

    public Optional<DedicatedSpawn> resolve(LocalSession owner) {
        UUID uniqueId = Objects.requireNonNull(owner, "owner").getUniqueId();

        DedicatedSpawn spawn = this.dedicated.get(uniqueId);
        if (spawn != null) {
            return Optional.of(spawn);
        } else if (!this.spawns.isEmpty()) {
            DedicatedSpawn dedicatedSpawn = new DedicatedSpawn(this.spawns.pop(), owner);
            this.dedicated.put(uniqueId, dedicatedSpawn);
            return Optional.of(dedicatedSpawn);
        }

        return Optional.empty();
    }
}
