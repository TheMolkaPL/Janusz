package pl.themolka.janusz.death;

import org.bukkit.entity.EntityType;
import pl.themolka.janusz.profile.Session;

public class PlayerKiller extends Killer {
    private final Session session;

    public PlayerKiller(Session session) {
        super(EntityType.PLAYER.getKey());
        this.session = session;
    }

    public Session getSession() {
        return this.session;
    }
}
