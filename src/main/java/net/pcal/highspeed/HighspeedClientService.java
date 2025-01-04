package net.pcal.highspeed;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.NewMinecartBehavior;
import net.minecraft.world.phys.Vec3;

import static java.util.Objects.requireNonNull;

public class HighspeedClientService implements ClientModInitializer {

    private static final int SPEEDOMETER_REFRESH_FREQUENCY = 4; // in ticks
    private int speedometerRefresh = 0;

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

    public void updateSpeedometer(final NewMinecartBehavior nmb, final AbstractMinecart minecart) {
        if (!this.isPlayerRiding(minecart)) return;
        if (speedometerRefresh++ < SPEEDOMETER_REFRESH_FREQUENCY) return;
        speedometerRefresh = 0;
        double distanceTraveled = 0;
        if (nmb.oldLerp != null && nmb.currentLerpSteps.size() > 2) {
            // iterate through the lerp steps to accurately calculate the distance travelled
            for (int i = 0; i < nmb.currentLerpSteps.size(); i++) {
                if (i == 0) {
                    distanceTraveled += nmb.oldLerp.position().distanceTo(nmb.currentLerpSteps.get(i).position());
                } else {
                    distanceTraveled += nmb.currentLerpSteps.get(i - 1).position().distanceTo(nmb.currentLerpSteps.get(i).position());
                }
            }
        }
        final int TICKS_PER_LERP = 3; //??
        double speed = distanceTraveled * 20 / TICKS_PER_LERP;
        final String display;
        if (speed < 10) {
            display = String.format("| %.1f bps |", speed);//, nmb.currentLerpSteps.size(), nmb.currentLerpStepsTotalWeight);
        } else {
            // not entirely clear why the -1 is necesary here but it makes the top speed line up with the maxSpeed
            display = String.format("| %d bps |", Math.round(speed) - 1); //, nmb.currentLerpSteps.size(), nmb.currentLerpStepsTotalWeight);
        }
        this.sendPlayerMessage(display);
    }
}
