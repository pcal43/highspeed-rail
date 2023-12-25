package net.pcal.highspeed.mixins;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.Boat;
import net.pcal.highspeed.HighspeedService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Boat.class)
public class BoatEntityMixin {

    @SuppressWarnings("InvalidInjectorMethodSignature")
    // seems to be a plugin bug here, incorrectly complains about var/return types
    @ModifyVariable(method = "getStatus", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/entity/vehicle/Boat;getGroundFriction()F"))
    protected float limitIceSlipperiness(float original) {
        return HighspeedService.getInstance().isIceBoatsEnabled() ? original : Mth.clamp(original, 0f, 0.45f);
    }
}