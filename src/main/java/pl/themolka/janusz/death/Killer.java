package pl.themolka.janusz.death;

import java.util.Objects;

public class Killer {
    private final String type;

    public Killer(String type) {
        this.type = Objects.requireNonNull(type, "type");
    }

    public String getType() {
        return this.type;
    }
}
