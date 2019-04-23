package pl.themolka.janusz.death;

import org.bukkit.NamespacedKey;

import java.util.Objects;

public class Killer {
    private final NamespacedKey type;

    public Killer(NamespacedKey type) {
        this.type = Objects.requireNonNull(type, "type");
    }

    public NamespacedKey getType() {
        return this.type;
    }
}
