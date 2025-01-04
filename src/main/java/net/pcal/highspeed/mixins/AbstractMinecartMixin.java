package net.pcal.highspeed.mixins;

import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.pcal.highspeed.HighspeedService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractMinecart.class)
public class AbstractMinecartMixin {

    @Inject(method = "useExperimentalMovement", at = @At("HEAD"), cancellable = true)
    private static void mf_useExperimentalMovement(Level level, CallbackInfoReturnable<Boolean> cir) {
        if (HighspeedService.getInstance().isNewMinecartPhysicsForceEnabled()) cir.setReturnValue(true);
    }
}
