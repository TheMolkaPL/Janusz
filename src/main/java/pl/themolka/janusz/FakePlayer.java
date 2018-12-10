package pl.themolka.janusz;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_13_R2.EntityPlayer;
import net.minecraft.server.v1_13_R2.MinecraftServer;
import net.minecraft.server.v1_13_R2.PlayerInteractManager;
import net.minecraft.server.v1_13_R2.WorldServer;

public class FakePlayer extends EntityPlayer {
    public FakePlayer(MinecraftServer minecraftserver,
                      WorldServer worldserver,
                      GameProfile gameprofile,
                      PlayerInteractManager playerinteractmanager) {
        super(minecraftserver, worldserver, gameprofile, playerinteractmanager);
    }

    @Override
    public void tick() {
    }

    @Override
    public void playerTick() {
    }
}
