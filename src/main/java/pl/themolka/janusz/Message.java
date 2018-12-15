package pl.themolka.janusz;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.util.Objects;

public class Message {
    private final String masculine;
    private final String feminine;
    private final String unisex;

    public Message(String masculin, String feminin, String unisex) {
        this.masculine = Objects.requireNonNull(masculin, "masculine");
        this.feminine = Objects.requireNonNull(feminin, "feminine");
        this.unisex = Objects.requireNonNull(unisex, "unisex");
    }

    public Message(String prefix, String masculin, String feminin, String unisex, String suffix) {
        this(Objects.requireNonNull(prefix, "prefix") + masculin + Objects.requireNonNull(suffix, "suffix"),
             prefix + feminin + suffix,
             prefix + unisex + suffix);
    }

    public String getMasculine() {
        return this.masculine;
    }

    public String getFeminine() {
        return this.feminine;
    }

    public String getUnisex() {
        return this.unisex;
    }

    public String format(CommandSender sender) {
        if (sender instanceof ConsoleCommandSender) {
            return this.unisex;
        }

        return this.format(sender.getName());
    }

    public String format(String username) {
        if (username.toLowerCase().equals("console")) {
            return this.unisex;
        }

        switch (username) {
            case "AsSik16":
            case "Monia97s":
            case "PizamaLama":
                return this.feminine;

            case "HejkaNaklejka":
            case "KubaSMT":
            case "Laki":
            case "Nerron":
            case "NoolNejm":
            case "SaveProjectAres":
            case "Tomasz":
            case "TheMolkaPL":
                return this.masculine;

            default:
                return this.unisex;
        }
    }
}
