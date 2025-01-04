package net.pcal.highspeed;

import java.util.Map;

import net.minecraft.resources.ResourceLocation;

public record HighspeedConfig(
        PerBlockConfig defaultBlockConfig,
        Map<ResourceLocation, PerBlockConfig> blockConfigs,
        boolean isSpeedometerEnabled,
        boolean isTrueSpeedometerEnabled,
        boolean isIceBoatsEnabled,
        boolean isExperimentalMovementForceEnabled
) {

    public record PerBlockConfig(
            Integer maxSpeed,
            Double boostAmount1,
            Double boostAmount2,
            Double boostThreshold,
            Double haltThreshold,
            Double haltScale,
            Double slowdownFactorOccupied,
            Double slowdownFactorEmpty
    ) {

    }
}
