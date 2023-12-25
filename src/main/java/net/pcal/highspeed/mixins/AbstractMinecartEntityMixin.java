package net.pcal.highspeed.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;
import net.pcal.highspeed.HighspeedClientService;
import net.pcal.highspeed.HighspeedService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractMinecart.class)
public abstract class AbstractMinecartEntityMixin {

    private static final double VANILLA_MAX_SPEED = 8.0 / 20.0;
    private static final double SQRT_TWO = 1.414213;

    private BlockPos lastPos = null;
    private double maxSpeed = VANILLA_MAX_SPEED;
    private double lastMaxSpeed = VANILLA_MAX_SPEED;
    private Vec3 lastSpeedPos = null;
    private long lastSpeedTime = 0;
    private final AbstractMinecart minecart = (AbstractMinecart) (Object) this;

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        updateSpeedometer();
        clampVelocity();
    }

    @Redirect(method = "moveAlongTrack", at = @At(value = "INVOKE", ordinal = 0, target = "java/lang/Math.min(DD)D"))
    public double speedClamp(double d1, double d2) {
        final double maxSpeed = getModifiedMaxSpeed();
        return maxSpeed == VANILLA_MAX_SPEED ? Math.min(d1, d2) // i.e. preserve vanilla behavior
                : Math.min(maxSpeed * SQRT_TWO, d2);
    }

    @Inject(method = "getMaxSpeed", at = @At("HEAD"), cancellable = true)
    protected void getMaxSpeed(CallbackInfoReturnable<Double> cir) {
        final double maxSpeed = getModifiedMaxSpeed();
        if (maxSpeed != VANILLA_MAX_SPEED) {
            cir.setReturnValue(maxSpeed);
        }
    }

    private double getModifiedMaxSpeed() {
        final BlockPos currentPos = minecart.blockPosition();
        if (currentPos.equals(lastPos)) return maxSpeed;
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
                return maxSpeed = VANILLA_MAX_SPEED;
            } else {
                final BlockState underState = minecart.level().getBlockState(currentPos.below());
                final ResourceLocation underBlockId = BuiltInRegistries.BLOCK.getKey(underState.getBlock());
                final Integer cartSpeedBps = HighspeedService.getInstance().getCartSpeed(underBlockId);
                if (cartSpeedBps != null) {
                    return maxSpeed = cartSpeedBps / 20.0;
                } else {
                    return maxSpeed = VANILLA_MAX_SPEED;
                }
            }
        } else {
            return maxSpeed = VANILLA_MAX_SPEED;
        }
    }

    private void clampVelocity() {
        if (getModifiedMaxSpeed() != lastMaxSpeed) {
            double smaller = Math.min(getModifiedMaxSpeed(), lastMaxSpeed);
            final Vec3 vel = minecart.getDeltaMovement();
            minecart.setDeltaMovement(new Vec3(Mth.clamp(vel.x, -smaller, smaller), 0.0,
                    Mth.clamp(vel.z, -smaller, smaller)));
        }
        lastMaxSpeed = maxSpeed;
    }

    private void updateSpeedometer() {
        final HighspeedService service = HighspeedService.getInstance();
        if (!service.isSpeedometerEnabled()) return;
        final AbstractMinecart minecart = (AbstractMinecart) (Object) this;
        if (!minecart.level().isClientSide) return;
        final HighspeedClientService client = service.getClientService();
        if (!client.isPlayerRiding(minecart)) return;
        final double override = getModifiedMaxSpeed();
        final Vec3 vel = minecart.getDeltaMovement();
        final Vec3 nominalVelocity = new Vec3(Mth.clamp(vel.x, -override, override), 0.0, Mth.clamp(vel.z, -override, override));
        final Double nominalSpeed = (nominalVelocity.horizontalDistance() * 20);
        final String display;
        if (!HighspeedService.getInstance().isSpeedometerTrueSpeedEnabled()) {
            display = String.format("| %.2f bps |", nominalSpeed);
        } else {
            final double trueSpeed;
            if (this.lastSpeedPos == null) {
                trueSpeed = 0.0;
                lastSpeedPos = client.getPlayerPos();
                lastSpeedTime = System.currentTimeMillis();
            } else {
                final long now = System.currentTimeMillis();
                final Vec3 playerPos = client.getPlayerPos();
                final Vec3 vector = playerPos.subtract(this.lastSpeedPos);
                trueSpeed = vector.horizontalDistance() * 1000 / ((now - lastSpeedTime));
                this.lastSpeedPos = playerPos;
                lastSpeedTime = now;
            }
            display = String.format("| %.2f bps %.2f  |", nominalSpeed, trueSpeed);
        }
        client.sendPlayerMessage(display);
    }
}

