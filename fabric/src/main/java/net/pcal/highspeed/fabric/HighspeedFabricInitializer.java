package net.pcal.highspeed.fabric;

import net.fabricmc.api.ModInitializer;
import net.pcal.highspeed.HighspeedService;

public class HighspeedFabricInitializer implements ModInitializer {

    @Override
    public void onInitialize() {
        HighspeedService.initialize();
    }
}
