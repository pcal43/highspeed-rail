package net.pcal.highspeed;

import java.util.List;
import net.minecraft.resources.ResourceLocation;

public record HighspeedConfig(
        List<HighspeedBlockConfig> blockConfigs,
        boolean isSpeedometerEnabled,
        boolean isTrueSpeedometerEnabled,
        boolean isIceBoatsEnabled
) {

    public record HighspeedBlockConfig(
            ResourceLocation blockId,
            int cartSpeed
    ) {
    }
}

