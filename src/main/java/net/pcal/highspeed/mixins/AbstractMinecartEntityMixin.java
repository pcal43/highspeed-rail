package net.pcal.highspeed.mixins;

import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.RailShape;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.pcal.highspeed.HighspeedService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractMinecartEntity.class)
public abstract class AbstractMinecartEntityMixin {

    private static final double VANILLA_MAX_SPEED = 8.0 / 20.0;
    private static final double SQRT_TWO = 1.414213;

    private BlockPos lastPos = null;
    private double maxSpeed = VANILLA_MAX_SPEED;
    private double lastMaxSpeed = VANILLA_MAX_SPEED;
    private Vec3d lastSpeedPos = null;
    private long lastSpeedTime = 0;
    private final AbstractMinecartEntity minecart = (AbstractMinecartEntity) (Object) this;

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        updateSpeedometer();
        clampVelocity();
    }

    @Redirect(method = "moveOnRail", at = @At(value = "INVOKE", ordinal = 0, target = "java/lang/Math.min(DD)D"))
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
        final BlockPos currentPos = minecart.getBlockPos();
        if (currentPos.equals(lastPos)) return maxSpeed;
        lastPos = currentPos;
        // look at the *next* block the cart is going to hit
        final Vec3d v = minecart.getVelocity();
        final BlockPos nextPos = new BlockPos(
                currentPos.getX() + MathHelper.sign(v.getX()),
                currentPos.getY(),
                currentPos.getZ() + MathHelper.sign(v.getZ())
        );
        final BlockState nextState = minecart.world.getBlockState(nextPos);
        if (nextState.getBlock() instanceof AbstractRailBlock rail) {
            final RailShape shape = nextState.get(rail.getShapeProperty());
            if (shape == RailShape.NORTH_EAST || shape == RailShape.NORTH_WEST || shape == RailShape.SOUTH_EAST || shape == RailShape.SOUTH_WEST) {
                return maxSpeed = VANILLA_MAX_SPEED;
            } else {
                final BlockState underState = minecart.world.getBlockState(currentPos.down());
                final Identifier underBlockId = Registry.BLOCK.getId(underState.getBlock());
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
            final Vec3d vel = minecart.getVelocity();
            minecart.setVelocity(new Vec3d(MathHelper.clamp(vel.x, -smaller, smaller), 0.0,
                    MathHelper.clamp(vel.z, -smaller, smaller)));
        }
        lastMaxSpeed = maxSpeed;
    }

    private void updateSpeedometer() {
        if (!HighspeedService.getInstance().isSpeedometerEnabled()) return;
        final AbstractMinecartEntity minecart = (AbstractMinecartEntity) (Object) this;
        if (!minecart.world.isClient) return;
        final ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null || player.getVehicle() != minecart) return;
        final double override = getModifiedMaxSpeed();
        final Vec3d vel = minecart.getVelocity();
        final Vec3d nominalVelocity = new Vec3d(MathHelper.clamp(vel.x, -override, override), 0.0, MathHelper.clamp(vel.z, -override, override));
        final Double nominalSpeed = (nominalVelocity.horizontalLength() * 20);
        final String display;
        if (!HighspeedService.getInstance().isSpeedometerTrueSpeedEnabled()) {
            display = String.format("| %.2f bps |", nominalSpeed);
        } else {
            final double trueSpeed;
            if (this.lastSpeedPos == null) {
                trueSpeed = 0.0;
                lastSpeedPos = player.getPos();
                lastSpeedTime = System.currentTimeMillis();
            } else {
                final long now = System.currentTimeMillis();
                final Vec3d playerPos = player.getPos();
                final Vec3d vector = playerPos.subtract(this.lastSpeedPos);
                trueSpeed = vector.horizontalLength() * 1000 / ((now - lastSpeedTime));
                this.lastSpeedPos = playerPos;
                lastSpeedTime = now;
            }
            display = String.format("| %.2f bps %.2f  |", nominalSpeed, trueSpeed);
        }
        player.sendMessage(Text.literal(display), true);
    }
}

