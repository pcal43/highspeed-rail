package net.pcal.highspeed;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import static java.util.Objects.requireNonNull;

public class HighspeedClientService implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        requireNonNull(HighspeedService.getInstance()).initClientService(this);
    }

    public boolean isPlayerRiding(AbstractMinecartEntity minecart) {
        final ClientPlayerEntity player = requireNonNull(MinecraftClient.getInstance().player);
        return player.getVehicle() == minecart;
    }

    public Vec3d getPlayerPos() {
        final ClientPlayerEntity player = requireNonNull(MinecraftClient.getInstance().player);
        return player.getPos();
    }

    public void sendPlayerMessage(String message) {
        final ClientPlayerEntity player = requireNonNull(MinecraftClient.getInstance().player);
        player.sendMessage(Text.literal(message), true);
    }


}
