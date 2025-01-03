package net.pcal.highspeed.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.MinecartBehavior;
import net.minecraft.world.entity.vehicle.NewMinecartBehavior;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.pcal.highspeed.HighspeedClientService;
import net.pcal.highspeed.HighspeedService;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Objects;

@Mixin(NewMinecartBehavior.class)
public abstract class NewMinecartBehaviorMixin {

    @Shadow @Final public List<NewMinecartBehavior.MinecartStep> currentLerpSteps;
    @Shadow public double currentLerpStepsTotalWeight;
    @Shadow public NewMinecartBehavior.MinecartStep oldLerp;
    private static final int VANILLA_MAX_BPS = 20;
    private final NewMinecartBehavior minecartBehavior = (NewMinecartBehavior) (Object) this;
    private final AbstractMinecart minecart = ((MinecartBehaviorAccessor) minecartBehavior).getMinecart();

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        if (((MinecartBehavior)(Object)this).level().isClientSide()) {
            updateSpeedometer();
        }
    }

    @Inject(method = "getMaxSpeed", at = @At("HEAD"), cancellable = true)
    protected void getMaxSpeed(CallbackInfoReturnable<Double> cir) {
        final BlockPos currentPos = minecart.blockPosition();
        final BlockState underState = minecart.level().getBlockState(currentPos.below());
        final ResourceLocation underBlockId = BuiltInRegistries.BLOCK.getKey(underState.getBlock());
        final Integer speedLimit = Objects.requireNonNullElse(HighspeedService.getInstance().getSpeedLimit(underBlockId), VANILLA_MAX_BPS);
        cir.setReturnValue(speedLimit * (this.minecart.isInWater() ? (double)0.5F : (double)1.0F) / (double)20.0F);
    }

    private void updateSpeedometer() {
        final HighspeedService service = HighspeedService.getInstance();
        if (!service.isSpeedometerEnabled()) return;
        final AbstractMinecart minecart = ((MinecartBehaviorAccessor) (NewMinecartBehavior) (Object) this).getMinecart();
        if (!minecart.level().isClientSide) return;
        final HighspeedClientService client = service.getClientService();
        if (!client.isPlayerRiding(minecart)) return;
        //final double override = getModifiedMaxSpeed();
        //final Vec3 nominalVelocity = new Vec3(Mth.clamp(vel.x, -override, override), 0.0, Mth.clamp(vel.z, -override, override));
        final Vec3 nominalVelocity;
        if (this.oldLerp != null && this.currentLerpSteps.size() > 2) {
            // This is interesting but wrong.  What we need to do is inject into calculateTrackSpeed(), calculate the speed there, accounting
            // for block configuration.
            nominalVelocity = this.oldLerp.position().subtract(this.currentLerpSteps.get(this.currentLerpSteps.size()-1).position());
            final int TICKS_PER_LERP = 3; //??
            double nominalSpeed = (nominalVelocity.horizontalDistance()) * 20 / TICKS_PER_LERP;
            nominalSpeed -= 1.2; // WTF IS THIS
            //nominalSpeed -= ((currentLerpSteps.size()) / currentLerpStepsTotalWeight); // WTF IS THIS
            String display = String.format("! %.2f bps ! lerps:%d weight:%.2f", nominalSpeed, currentLerpSteps.size(), currentLerpStepsTotalWeight);
            client.sendPlayerMessage(display);
        } else {
            nominalVelocity = Vec3.ZERO;//minecart.getDeltaMovement();
            final Double nominalSpeed = (nominalVelocity.horizontalDistance() * 20);
            String display = String.format("? %.2f bps ?", nominalSpeed);
            client.sendPlayerMessage(display);
        }

    }
}

