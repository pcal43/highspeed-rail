package net.pcal.highspeed.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.NewMinecartBehavior;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.pcal.highspeed.HighspeedService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NewMinecartBehavior.class)
public abstract class NewMinecartBehaviorMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        final AbstractMinecart minecart = ((MinecartBehaviorAccessor) this).getMinecart();
        if (!minecart.level().isClientSide()) return;
        final HighspeedService service = HighspeedService.getInstance();
        if (!service.isSpeedometerEnabled()) return;
        HighspeedService.getInstance().getClientService().updateSpeedometer((NewMinecartBehavior) (Object)this, minecart);
    }

    @Inject(method = "getMaxSpeed", at = @At("HEAD"), cancellable = true)
    protected void getMaxSpeed(CallbackInfoReturnable<Double> cir) {
        final Double customMaxSpeed = HighspeedService.getInstance().getMaxSpeed(
                (NewMinecartBehavior) (Object)this, ((MinecartBehaviorAccessor) this).getMinecart()
        );
        if (customMaxSpeed != null) cir.setReturnValue(customMaxSpeed);
    }

    @Inject(method = "getSlowdownFactor", at = @At("HEAD"), cancellable = true)
    protected void getSlowdownFactor(CallbackInfoReturnable<Double> cir) {
        final Double customSlowdownFactor = HighspeedService.getInstance().getSlowdownFactor(
                (NewMinecartBehavior) (Object)this, ((MinecartBehaviorAccessor) this).getMinecart()
        );
        if (customSlowdownFactor != null) cir.setReturnValue(customSlowdownFactor);
    }

    @ModifyConstant(
        method = "calculateBoostTrackSpeed",
        constant = @Constant(doubleValue = 0.01)
    )
    private double boostSlowThreshold(double defaultValue, @Local(argsOnly = true) BlockPos blockPos) {
        return HighspeedService.getInstance().calculateBoostSlowThreshold(
            defaultValue, ((MinecartBehaviorAccessor) this).getMinecart(), blockPos
        );
    }

    @ModifyConstant(
        method = "calculateBoostTrackSpeed",
        constant = @Constant(doubleValue = 0.06)
    )
    private double boostFactor(double defaultValue, @Local(argsOnly = true) BlockPos blockPos) {
        return HighspeedService.getInstance().calculateBoostFactor(
            defaultValue, ((MinecartBehaviorAccessor) this).getMinecart(), blockPos
        );
    }

    @ModifyConstant(
        method = "calculateBoostTrackSpeed",
        constant = @Constant(doubleValue = 0.2)
    )
    private double boostSlowFactor(double defaultValue, @Local(argsOnly = true) BlockPos blockPos) {
        return HighspeedService.getInstance().calculateBoostSlowFactor(
            defaultValue, ((MinecartBehaviorAccessor) this).getMinecart(), blockPos
        );
    }

    @Inject(method = "calculateHaltTrackSpeed", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/world/phys/Vec3;length()D"), cancellable = true)
    private void calculateHaltTrackSpeed(Vec3 vec3, BlockState blockState, CallbackInfoReturnable<Vec3> cir) {
        final Vec3 customHaltSpeed = HighspeedService.getInstance().calculateHaltTrackSpeed(
                (NewMinecartBehavior) (Object)this, ((MinecartBehaviorAccessor) this).getMinecart(), vec3, blockState
        );
        if (customHaltSpeed != null) cir.setReturnValue(customHaltSpeed);
    }
}
