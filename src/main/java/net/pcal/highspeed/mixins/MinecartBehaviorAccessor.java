package net.pcal.highspeed.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.MinecartBehavior;

@Mixin(MinecartBehavior.class)
public interface MinecartBehaviorAccessor {
    
    @Accessor("minecart")
    AbstractMinecart getMinecart();
}