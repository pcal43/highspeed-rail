package net.pcal.highspeed;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.phys.Vec3;

import static java.util.Objects.requireNonNull;

public class HighspeedClientService implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        requireNonNull(HighspeedService.getInstance()).initClientService(this);
    }

    public boolean isPlayerRiding(AbstractMinecart minecart) {
        final LocalPlayer player = requireNonNull(Minecraft.getInstance().player);
        return player.getVehicle() == minecart;
    }

    public Vec3 getPlayerPos() {
        final LocalPlayer player = requireNonNull(Minecraft.getInstance().player);
        return player.position();
    }

    public void sendPlayerMessage(String message) {
        final LocalPlayer player = requireNonNull(Minecraft.getInstance().player);
        player.displayClientMessage(Component.literal(message), true);
    }
}
