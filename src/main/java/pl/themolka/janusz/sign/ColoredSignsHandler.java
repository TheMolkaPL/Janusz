package pl.themolka.janusz.sign;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;
import pl.themolka.janusz.JanuszPlugin;

import java.util.Objects;

public class ColoredSignsHandler extends JanuszPlugin.Handler {
    private static final String PERMISSION = "janusz.colored-signs";
    private static final char COLOR_CODE = '&';

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void translateColors(SignChangeEvent event) {
        this.translateColors(event.getLines(), event.getPlayer());
    }

    public void translateColors(String[] lines, Player writer) {
        if (Objects.requireNonNull(writer, "writer").hasPermission(PERMISSION)) {
            for (int i = 0; i < lines.length; i++) {
                lines[i] = ChatColor.translateAlternateColorCodes(COLOR_CODE, lines[i]);
            }
        }
    }
}
