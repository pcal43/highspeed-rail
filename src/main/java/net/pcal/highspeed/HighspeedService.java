package net.pcal.highspeed;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.NewMinecartBehavior;
import net.minecraft.world.entity.vehicle.OldMinecartBehavior;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.pcal.highspeed.HighspeedConfig.PerBlockConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

public class HighspeedService implements ModInitializer {

    // ===================================================================================
    // Singleton

    private static HighspeedService INSTANCE = null;
    private HighspeedConfig config;
    private HighspeedClientService clientService;

    private final Logger logger = LogManager.getLogger("HighspeedRail");

    public static HighspeedService getInstance() {
        return requireNonNull(INSTANCE);
    }

    // ===================================================================================
    // Constants

    private static final String CONFIG_RESOURCE_NAME = "net/pcal/highspeed/default-config.json5";
    private static final String CONFIG_FILENAME = "highspeed-rail.json5";
    private static final Path CONFIG_FILE_PATH = Paths.get("config", CONFIG_FILENAME);

    // ===================================================================================
    // ModInitializer implementation

    @Override
    public void onInitialize() {
        if (!CONFIG_FILE_PATH.toFile().exists()) {
            try (InputStream in = this.getClass().getClassLoader().getResourceAsStream(CONFIG_RESOURCE_NAME)) {
                if (in == null) throw new IllegalStateException("Unable to load " + CONFIG_RESOURCE_NAME);
                java.nio.file.Files.createDirectories(CONFIG_FILE_PATH); // dir doesn't exist on fresh install
                java.nio.file.Files.copy(in, CONFIG_FILE_PATH, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try (final InputStream in = new FileInputStream(CONFIG_FILE_PATH.toFile())) {
            this.config = HighspeedConfigParser.parse(in);
            this.logger.info("Configuration loaded from " + CONFIG_FILE_PATH.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (this.config.isNewMinecartPhysicsForceEnabled()) {
            this.logger.warn("Experimental minecart movement is force-enabled.  This may cause unexpected behavior.");
        }
        if (INSTANCE != null) throw new IllegalStateException();
        INSTANCE = this;
    }

    // ===================================================================================
    // OldMinecartBehavior support

    /**
     * @return the maximum speed (in blocks-per-second) that a cart travelling on a rail sitting
     * on the given block type can travel at.  Returns null if the vanilla default should be used.
     *
     * FIXME this should be a Double.
     */
    public Integer getOldMaxSpeed(OldMinecartBehavior omb, AbstractMinecart minecart, ResourceLocation blockId) {
        final PerBlockConfig pbc = this.getPerBlockConfig(minecart);
        return pbc == null ? null : pbc.oldMaxSpeed();
    }

    // ===================================================================================
    // NewMinecartBehavior support

    public boolean isNewMinecartPhysicsForceEnabled() {
        return this.config.isNewMinecartPhysicsForceEnabled();
    }

    public Double getMaxSpeed(NewMinecartBehavior nmb, AbstractMinecart minecart) {
        final PerBlockConfig pbc = this.getPerBlockConfig(minecart);
        if (pbc == null) return null;
        return (double)requireNonNullElse(pbc.maxSpeed(), 20) * (minecart.isInWater() ? (double) 0.5F : (double) 1.0F) / (double) 20.0F;
    }

    public Double getSlowdownFactor(NewMinecartBehavior nmb, AbstractMinecart minecart) {
        final PerBlockConfig pbc = this.getPerBlockConfig(minecart);
        if (pbc == null) return null;
        return minecart.isVehicle() ?
                requireNonNullElse(pbc.slowdownFactorOccupied(), 0.997) :
                requireNonNullElse(pbc.slowdownFactorEmpty(), 0.975);
    }

    public Vec3 calculateBoostTrackSpeed(NewMinecartBehavior nmb, AbstractMinecart minecart, Vec3 vec3, BlockPos blockPos, BlockState blockState) {
        if (blockState.is(Blocks.POWERED_RAIL) && (Boolean) blockState.getValue(PoweredRailBlock.POWERED)) {
            final PerBlockConfig pbc = this.getPerBlockConfig(minecart, blockPos);
            if (pbc == null) return null;
            if (vec3.length() > requireNonNullElse(pbc.boostSlowThreshold(), 0.01)) {
                return vec3.normalize().scale(vec3.length() + requireNonNullElse(pbc.boostFactor(), 0.06));
            } else {
                Vec3 vec32 = minecart.getRedstoneDirection(blockPos);
                return vec32.lengthSqr() <= (double) 0.0F ? vec3 : vec32.scale(vec3.length() + requireNonNullElse(pbc.boostSlowFactor(), 0.2));
            }
        } else {
            return vec3; // this would be the vanilla result
        }
    }

    public Vec3 calculateHaltTrackSpeed(NewMinecartBehavior nmb, AbstractMinecart minecart, Vec3 vec3, BlockState blockState) {
        if (blockState.is(Blocks.POWERED_RAIL) && !(Boolean) blockState.getValue(PoweredRailBlock.POWERED)) {
            final PerBlockConfig pbc = this.getPerBlockConfig(minecart);
            if (pbc == null) return null;
            return vec3.length() < requireNonNullElse(pbc.haltThreshold(), 0.03) ? Vec3.ZERO : vec3.scale(requireNonNullElse(pbc.haltFactor(), 0.5));
        } else {
            return vec3;
        }
    }

    // ===================================================================================
    // Client-specific support.  This need to be a separate mod

    public void initClientService(HighspeedClientService clientService) {
        if (this.clientService != null) throw new IllegalStateException();
        this.clientService = requireNonNull(clientService);
    }

    public boolean isSpeedometerEnabled() {
        return this.config.isSpeedometerEnabled();
    }

    public boolean isSpeedometerTrueSpeedEnabled() {
        return this.config.isTrueSpeedometerEnabled();
    }

    public boolean isIceBoatsEnabled() {
        return this.config.isIceBoatsEnabled();
    }

    public HighspeedClientService getClientService() {
        if (this.clientService == null) throw new UnsupportedOperationException("clientService not initialized");
        return this.clientService;
    }

    // ===================================================================================
    // Private

    private PerBlockConfig getPerBlockConfig(AbstractMinecart minecart) {
        return getPerBlockConfig(minecart, minecart.getCurrentBlockPosOrRailBelow());
    }

    private PerBlockConfig getPerBlockConfig(AbstractMinecart minecart, BlockPos minecartPos) {
        if (this.config.blockConfigs() == null) return this.config.defaultBlockConfig();
        final BlockState underState = minecart.level().getBlockState(minecartPos.below());
        final ResourceLocation underBlockId = BuiltInRegistries.BLOCK.getKey(underState.getBlock());
        final PerBlockConfig pbc = this.config.blockConfigs().get(underBlockId);
        return pbc != null ? pbc :  this.config.defaultBlockConfig();
    }
}
