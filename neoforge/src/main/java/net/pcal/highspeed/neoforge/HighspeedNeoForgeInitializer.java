package net.pcal.highspeed.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.pcal.highspeed.HighspeedService;

@Mod(HighspeedNeoForgeInitializer.MOD_ID)
public class HighspeedNeoForgeInitializer {

    public static final String MOD_ID = "highspeed_rail";

    public HighspeedNeoForgeInitializer() {
        HighspeedService.initialize();
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            initializeClientService();
        }
    }

    private static void initializeClientService() {
        try {
            Class.forName("net.pcal.highspeed.HighspeedClientService").getMethod("initialize").invoke(null);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
