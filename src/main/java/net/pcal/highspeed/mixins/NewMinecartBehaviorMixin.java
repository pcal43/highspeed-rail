package net.pcal.highspeed.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.NewMinecartBehavior;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.pcal.highspeed.HighspeedService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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

    @Inject(method = "calculateBoostTrackSpeed", at = @At("HEAD"), cancellable = true)
    private void calculateBoostTrackSpeed(Vec3 vec3, BlockPos blockPos, BlockState blockState,  CallbackInfoReturnable<Vec3> cir) {
        final Vec3 customBoostSpeed = HighspeedService.getInstance().calculateBoostTrackSpeed(
                (NewMinecartBehavior) (Object)this, ((MinecartBehaviorAccessor) this).getMinecart(), vec3, blockPos, blockState
        );
        if (customBoostSpeed != null) cir.setReturnValue(customBoostSpeed);
    }

    @Inject(method = "calculateHaltTrackSpeed", at = @At("HEAD"), cancellable = true)
    private void calculateHaltTrackSpeed(Vec3 vec3, BlockState blockState, CallbackInfoReturnable<Vec3> cir) {
        final Vec3 customHaltSpeed = HighspeedService.getInstance().calculateHaltTrackSpeed(
                (NewMinecartBehavior) (Object)this, ((MinecartBehaviorAccessor) this).getMinecart(), vec3, blockState
        );
        if (customHaltSpeed != null) cir.setReturnValue(customHaltSpeed);
    }
}
