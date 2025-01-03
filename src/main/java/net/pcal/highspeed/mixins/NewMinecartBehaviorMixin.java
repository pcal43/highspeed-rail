package net.pcal.highspeed.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.MinecartBehavior;
import net.minecraft.world.entity.vehicle.NewMinecartBehavior;

import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;
import net.pcal.highspeed.HighspeedClientService;
import net.pcal.highspeed.HighspeedService;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(NewMinecartBehavior.class)
public abstract class NewMinecartBehaviorMixin {

    @Shadow @Final public List<NewMinecartBehavior.MinecartStep> currentLerpSteps;
    @Shadow public double currentLerpStepsTotalWeight;
    @Shadow public NewMinecartBehavior.MinecartStep oldLerp;
    private static final double VANILLA_MAX_SPEED = 8.0 / 20.0;
    private static final double SQRT_TWO = 1.414213;

    private BlockPos lastPos = null;
    private double currentMaxSpeed = VANILLA_MAX_SPEED;
    private double lastMaxSpeed = VANILLA_MAX_SPEED;
    private Vec3 lastSpeedPos = null;
    private long lastSpeedTime = 0;
    private final NewMinecartBehavior minecartBehavior = (NewMinecartBehavior) (Object) this;
    private final AbstractMinecart minecart = ((MinecartBehaviorAccessor) minecartBehavior).getMinecart();

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        Level level = ((MinecartBehavior)(Object)this).level();
        if (level instanceof ServerLevel serverLevel) {

        } else {
            updateSpeedometer();
        }
        //clampVelocity();
    }

    /**
     @Redirect(method = "moveAlongTrack", at = @At(value = "INVOKE", ordinal = 0, target = "java/lang/Math.min(DD)D"))
    public double speedClamp(double d1, double d2) {
        final double maxSpeed = getModifiedMaxSpeed();
        return maxSpeed == VANILLA_MAX_SPEED ? Math.min(d1, d2) // i.e. preserve vanilla behavior
                : Math.min(maxSpeed * SQRT_TWO, d2);
    }
    **/

    /**
     @Inject(method = "getMaxSpeed", at = @At("HEAD"), cancellable = true)
     protected void getMaxSpeed(CallbackInfoReturnable<Double> cir) {
        final double maxSpeed = getModifiedMaxSpeed();
        if (maxSpeed != VANILLA_MAX_SPEED) {
            cir.setReturnValue(maxSpeed);
        }
    }
**/
    /*
    private double getModifiedMaxSpeed() {
        final BlockPos currentPos = minecart.blockPosition();
        if (currentPos.equals(lastPos)) return currentMaxSpeed;
        lastPos = currentPos;
        // look at the *next* block the cart is going to hit
        final Vec3 v = minecart.getDeltaMovement();
        final BlockPos nextPos = new BlockPos(
                currentPos.getX() + Mth.sign(v.x()),
                currentPos.getY(),
                currentPos.getZ() + Mth.sign(v.z())
        );
        final BlockState nextState = minecart.level().getBlockState(nextPos);
        if (nextState.getBlock() instanceof BaseRailBlock rail) {
            final RailShape shape = nextState.getValue(rail.getShapeProperty());
            if (shape == RailShape.NORTH_EAST || shape == RailShape.NORTH_WEST || shape == RailShape.SOUTH_EAST || shape == RailShape.SOUTH_WEST) {
                return currentMaxSpeed = VANILLA_MAX_SPEED;
            } else {
                final BlockState underState = minecart.level().getBlockState(currentPos.below());
                final ResourceLocation underBlockId = BuiltInRegistries.BLOCK.getKey(underState.getBlock());
                final Integer speedLimit = HighspeedService.getInstance().getSpeedLimit(underBlockId);
                if (speedLimit != null) {
                    return currentMaxSpeed = speedLimit / 20.0;
                } else {
                    return currentMaxSpeed = VANILLA_MAX_SPEED;
                }
            }
        } else {
            return currentMaxSpeed = VANILLA_MAX_SPEED;
        }
    }

    private void clampVelocity() {
        if (getModifiedMaxSpeed() != lastMaxSpeed) {
            double smaller = Math.min(getModifiedMaxSpeed(), lastMaxSpeed);
            final Vec3 vel = minecart.getDeltaMovement();
            minecart.setDeltaMovement(new Vec3(Mth.clamp(vel.x, -smaller, smaller), 0.0,
                    Mth.clamp(vel.z, -smaller, smaller)));
        }
        lastMaxSpeed = currentMaxSpeed;
    }
**/

    @Inject(method = "getMaxSpeed", at = @At("HEAD"), cancellable = true)
    protected void getMaxSpeed(CallbackInfoReturnable<Double> cir) {
        double blockSpeed = 83.0;
        cir.setReturnValue(blockSpeed * (this.minecart.isInWater() ? (double)0.5F : (double)1.0F) / (double)20.0F);
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

