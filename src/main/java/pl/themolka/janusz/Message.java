package pl.themolka.janusz;

import org.bukkit.ChatColor;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

public enum Message {
    JOIN(ChatColor.AQUA + "%s " + ChatColor.DARK_AQUA,
            "dołączył", "dołączyła", "dołączył/a",
            " do serwera"),
    QUIT(ChatColor.AQUA + "%s " + ChatColor.DARK_AQUA,
            "wyszedł", "wyszła", "opuścił/a",
            " serwer"),
    ;

    private final String masculin;
    private final String feminin;
    private final String unisex;

    Message(String masculin, String feminin, String unisex) {
        this.masculin = masculin;
        this.feminin = feminin;
        this.unisex = unisex;
    }

    Message(String prefix, String masculin, String feminin, String unisex, String suffix) {
        this(prefix + masculin + suffix,
             prefix + feminin + suffix,
             prefix + unisex + suffix);
    }

    public String getMasculin() {
        return this.masculin;
    }

    public String getFeminin() {
        return this.feminin;
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
        if (username.equals(JanuszPlugin.PROFILE_USERNAME)) {
            return null;
        } else if (username.toLowerCase().equals("console")) {
            return this.unisex;
        }

        switch (username) {
            case "AsSik16":
            case "Monia97s":
            case "PizamaLama":
                return this.feminin;

            case "HejkaNaklejka":
            case "Nerron":
            case "NoolNejm":
            case "SaveProjectAres":
            case "TheMolkaPL":
                return this.masculin;

            default:
                return this.unisex;
        }
    }
}
