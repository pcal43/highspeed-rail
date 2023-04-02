package net.pcal.highspeed;

import net.minecraft.util.Identifier;

import java.util.List;

public record HighspeedConfig(
        List<HighspeedBlockConfig> blockConfigs,
        boolean isSpeedometerEnabled,
        boolean isTrueSpeedometerEnabled,
        boolean isIceBoatsEnabled
) {

    public record HighspeedBlockConfig(
            Identifier blockId,
            int cartSpeed
    ) {
    }
}

