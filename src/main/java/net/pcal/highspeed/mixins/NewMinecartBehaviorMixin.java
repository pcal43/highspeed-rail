package net.pcal.highspeed.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.NewMinecartBehavior;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.pcal.highspeed.HighspeedClientService;
import net.pcal.highspeed.HighspeedService;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Objects;

@Mixin(NewMinecartBehavior.class)
public abstract class NewMinecartBehaviorMixin {

    @Shadow
    @Final
    public List<NewMinecartBehavior.MinecartStep> currentLerpSteps;
    @Shadow
    public double currentLerpStepsTotalWeight;
    @Shadow
    public NewMinecartBehavior.MinecartStep oldLerp;
    private static final int VANILLA_MAX_BPS = 20;
    private final NewMinecartBehavior minecartBehavior = (NewMinecartBehavior) (Object) this;

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        updateSpeedometer();
    }

    @Inject(method = "getMaxSpeed", at = @At("HEAD"), cancellable = true)
    protected void getMaxSpeed(CallbackInfoReturnable<Double> cir) {
        final AbstractMinecart minecart = ((MinecartBehaviorAccessor) this).getMinecart();
        final BlockPos currentPos = minecart.blockPosition();
        final BlockState underState = minecart.level().getBlockState(currentPos.below());
        final ResourceLocation underBlockId = BuiltInRegistries.BLOCK.getKey(underState.getBlock());
        final Integer speedLimit = Objects.requireNonNullElse(HighspeedService.getInstance().getSpeedLimit(underBlockId), VANILLA_MAX_BPS);
        cir.setReturnValue(speedLimit * (minecart.isInWater() ? (double) 0.5F : (double) 1.0F) / (double) 20.0F);
    }

    @Unique
    private void updateSpeedometer() {
        final AbstractMinecart minecart = ((MinecartBehaviorAccessor) this).getMinecart();
        if (!minecart.level().isClientSide()) return;
        final HighspeedService service = HighspeedService.getInstance();
        if (!service.isSpeedometerEnabled()) return;
        final HighspeedClientService client = service.getClientService();
        if (!client.isPlayerRiding(minecart)) return;
        double distanceTraveled = 0;
        if (this.oldLerp != null && this.currentLerpSteps.size() > 2) {
            // iterate through the lerp steps and calculate the speed
            for (int i = 1; i < currentLerpSteps.size(); i++) {
                distanceTraveled += currentLerpSteps.get(i - 1).position().distanceTo(currentLerpSteps.get(i).position());
            }
        }
        final int TICKS_PER_LERP = 3; //??
        double speed = distanceTraveled * 20 / TICKS_PER_LERP;
        speed -= 1.2; // WTF IS THIS
        //nominalSpeed -= ((currentLerpSteps.size()) / currentLerpStepsTotalWeight); // WTF IS THIS
        String display = String.format("! %.2f bps ! lerps:%d weight:%.2f", speed, currentLerpSteps.size(), currentLerpStepsTotalWeight);
        client.sendPlayerMessage(display);
    }
}


