package net.pcal.highspeed;

import java.util.Map;

import net.minecraft.resources.ResourceLocation;

public record HighspeedConfig(
        PerBlockConfig defaultBlockConfig,
        Map<ResourceLocation, PerBlockConfig> blockConfigs,
        boolean isSpeedometerEnabled,
        boolean isTrueSpeedometerEnabled,
        boolean isIceBoatsEnabled,
        boolean isNewMinecartPhysicsForceEnabled
) {

    public record PerBlockConfig(
            Integer oldMaxSpeed,
            Integer maxSpeed,
            Double boostFactor,
            Double boostSlowFactor,
            Double boostSlowThreshold,
            Double haltThreshold,
            Double haltFactor,
            Double slowdownFactorOccupied,
            Double slowdownFactorEmpty
    ) {

    }
}
