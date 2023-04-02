package net.pcal.highspeed;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static java.util.Objects.requireNonNull;

public class HighspeedService implements ModInitializer {

    // ===================================================================================
    // Singleton

    private static HighspeedService INSTANCE = null;
    private HighspeedConfig config;
    private HighspeedClientService clientDelegate;

    public static HighspeedService getInstance() {
        return requireNonNull(INSTANCE);
    }

    // ===================================================================================
    // Constants

    private static final String CONFIG_RESOURCE_NAME = "default-config.json5";
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
        if (INSTANCE != null) throw new IllegalStateException();
        INSTANCE = this;
    }

    public void initClientDelegate(HighspeedClientService clientDelegate) {
        if (this.clientDelegate != null) throw new IllegalStateException();
        this.clientDelegate = requireNonNull(clientDelegate);
    }

    // ===================================================================================
    // Public methods

    public Integer getCartSpeed(Identifier id) {
        for (HighspeedConfig.HighspeedBlockConfig bc : this.config.blockConfigs()) {
            if (id.equals(bc.blockId())) return bc.cartSpeed();
        }
        return null;
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

    public HighspeedClientService getClientDelegate() {
        if (this.clientDelegate == null) throw new UnsupportedOperationException("clientDelegate not initialized");
        return this.clientDelegate;
    }

}
