package net.pcal.highspeed.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.pcal.highspeed.HighspeedClientService;

public class HighspeedFabricClientInitializer implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HighspeedClientService.initialize();
    }
}
