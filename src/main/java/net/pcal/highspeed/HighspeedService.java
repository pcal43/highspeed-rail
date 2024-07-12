package net.pcal.highspeed;

import com.google.common.collect.ImmutableMap;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class HighspeedService implements ModInitializer {

    // ===================================================================================
    // Singleton

    private static HighspeedService INSTANCE = null;
    private HighspeedConfig config;
    private HighspeedClientService clientService;
    private Map<ResourceLocation, Integer> speedLimitPerBlock;

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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final ImmutableMap.Builder<ResourceLocation, Integer> b = ImmutableMap.builder();
        this.config.blockConfigs().forEach(bc->b.put(bc.blockId(), bc.speedLimit()));
        this.speedLimitPerBlock = b.build();

        if (INSTANCE != null) throw new IllegalStateException();
        INSTANCE = this;
    }

    public void initClientService(HighspeedClientService clientService) {
        if (this.clientService != null) throw new IllegalStateException();
        this.clientService = requireNonNull(clientService);
    }

    // ===================================================================================
    // Public methods

    /**
     * @return the maximum speed (in blocks-per-second) that a cart travelling on a rail sitting
     * on the given block type can travel at.  Returns null if the vanilla default should be used.
     */
    public Integer getSpeedLimit(ResourceLocation blockId) {
        return this.speedLimitPerBlock.getOrDefault(blockId, this.config.defaultSpeedLimit());
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
}
