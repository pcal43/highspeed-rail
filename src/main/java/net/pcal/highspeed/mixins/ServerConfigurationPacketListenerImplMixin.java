package net.pcal.highspeed.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.pcal.highspeed.HighspeedService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerConfigurationPacketListenerImpl.class)
public abstract class ServerConfigurationPacketListenerImplMixin {
    @Unique
    private static final FeatureFlagSet minecartFeatureFlagSet = FeatureFlagSet.of(FeatureFlags.MINECART_IMPROVEMENTS);

    @ModifyExpressionValue(method = "startConfiguration", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/WorldData;enabledFeatures()Lnet/minecraft/world/flag/FeatureFlagSet;"))
    private FeatureFlagSet fakeEnableMinecartExperiment(FeatureFlagSet featureFlagSet) {
        if (HighspeedService.getInstance().isNewMinecartPhysicsForceEnabled()) {
            featureFlagSet = featureFlagSet.join(minecartFeatureFlagSet);
        }
        return featureFlagSet;
    }
}
