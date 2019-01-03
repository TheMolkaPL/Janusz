package pl.themolka.janusz.arena.sign;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.event.player.PlayerInteractEvent;
import pl.themolka.janusz.Message;
import pl.themolka.janusz.arena.Arena;
import pl.themolka.janusz.arena.Game;
import pl.themolka.janusz.arena.GameState;
import pl.themolka.janusz.arena.Idle;
import pl.themolka.janusz.arena.Match;
import pl.themolka.janusz.arena.Starting;
import pl.themolka.janusz.geometry.Vector3d;
import pl.themolka.janusz.profile.LocalSession;

public class JoinSign extends ArenaSign {
    private static final Message OK = new Message("Dołączył", "eś", "aś", "eś/aś", " do kolejki do gry.");
    private static final Message FAIL = new Message("Nie udało się dołączyć do kolejki do gry - spróbuj ponownie później!");

    public JoinSign(Arena arena, Vector3d location) {
        super(arena, location);
    }

    @Override
    public void click(Game game, LocalSession clicker, PlayerInteractEvent event) {
        super.click(game, clicker, event);

        if (game.canJoin(clicker)) {
            if (game.join(clicker)) {
                clicker.printSuccess(OK);
            } else {
                clicker.printError(FAIL);
            }
        }
    }

    @Override
    public void update(Game game, Sign sign) {
        super.update(game, sign);

        GameState state = game.getState();

        String info;
        if (state instanceof Idle) {
            info = ChatColor.AQUA + "Oczekiwanie...";
        } else if (state instanceof Starting) {
            info = ChatColor.GREEN + "Startowanie...";
        } else if (state instanceof Match) {
            info = ChatColor.DARK_RED + "W grze";
        } else {
            info = "";
        }

        sign.setLine(0, ChatColor.GREEN + ChatColor.UNDERLINE.toString() + "[Dołącz]");
        sign.setLine(1, this.getArena().getName());
        sign.setLine(2, "");
        sign.setLine(3, info);
    }
}
