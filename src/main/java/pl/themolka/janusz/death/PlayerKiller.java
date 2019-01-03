package pl.themolka.janusz.death;

import net.minecraft.server.v1_13_R2.EntityTypes;
import pl.themolka.janusz.profile.Session;

import java.util.Objects;

public class PlayerKiller extends Killer {
    private static final String ID = Objects.requireNonNull(EntityTypes.getName(EntityTypes.PLAYER)).toString();

    private final Session session;

    public PlayerKiller(Session session) {
        super(ID);
        this.session = session;
    }

    public Session getSession() {
        return this.session;
    }
}
