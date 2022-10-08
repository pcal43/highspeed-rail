package net.pcal.highspeed.mixins;

import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.MathHelper;
import net.pcal.highspeed.HighspeedService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BoatEntity.class)
public class BoatEntityMixin {

    @SuppressWarnings("InvalidInjectorMethodSignature")
    // seems to be a plugin bug here, incorrectly complains about var/return types
    @ModifyVariable(method = "checkLocation", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/entity/vehicle/BoatEntity;getNearbySlipperiness()F"))
    protected float limitIceSlipperiness(float original) {
        return HighspeedService.getInstance().isIceBoatsEnabled() ? original : MathHelper.clamp(original, 0f, 0.45f);
    }
}