package pl.themolka.janusz;

import java.util.Objects;

public class Message {
    private final String masculine;
    private final String feminine;
    private final String unisex;

    public Message(String unisex) {
        this(unisex, unisex, unisex);
    }

    public Message(String masculine, String feminine, String unisex) {
        this.masculine = Objects.requireNonNull(masculine, "masculine");
        this.feminine = Objects.requireNonNull(feminine, "feminine");
        this.unisex = Objects.requireNonNull(unisex, "unisex");
    }

    public Message(String prefix,
                   String masculine, String feminine, String unisex,
                   String suffix) {
        this(Objects.requireNonNull(prefix, "prefix") + Objects.requireNonNull(masculine, "masculine") + Objects.requireNonNull(suffix, "suffix"),
             prefix + Objects.requireNonNull(feminine, "feminine") + suffix,
             prefix + Objects.requireNonNull(unisex, "unisex") + suffix);
    }

    public String masculine() {
        return this.masculine;
    }

    public String feminine() {
        return this.feminine;
    }

    public String unisex() {
        return this.unisex;
    }
}
