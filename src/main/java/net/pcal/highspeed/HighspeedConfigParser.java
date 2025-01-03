package net.pcal.highspeed;

import com.google.gson.Gson;
import net.minecraft.resources.ResourceLocation;
import net.pcal.highspeed.HighspeedConfig.HighspeedBlockConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

class HighspeedConfigParser {

    static HighspeedConfig parse(final InputStream in) throws IOException {
        final List<HighspeedBlockConfig> blocks = new ArrayList<>();
        final String rawJson = stripComments(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        final Gson gson = new Gson();
        final HighspeedConfigGson configGson = gson.fromJson(rawJson, HighspeedConfigGson.class);
        for (HighspeedBlockConfigGson blockGson : configGson.blocks) {
            HighspeedBlockConfig bc = new HighspeedBlockConfig(
                    ResourceLocation.parse(requireNonNull(blockGson.blockId, "blockId is required")),
                    blockGson.cartSpeed != null ? blockGson.cartSpeed : blockGson.speedLimit
            );
            blocks.add(bc);
        }
        // adjust logging to configured level
        return new HighspeedConfig(
                Collections.unmodifiableList(blocks),
                requireNonNullElse(configGson.isSpeedometerEnabled,true),
                requireNonNullElse(configGson.isTrueSpeedometerEnabled, false),
                requireNonNullElse(configGson.isIceBoatsEnabled, false),
                requireNonNullElse(configGson.isExperimentalMovementForceEnabled, false),
                configGson.defaultSpeedLimit // may be null
        );
    }

    // ===================================================================================
    // Private methods

    private static String stripComments(String json) throws IOException {
        final StringBuilder out = new StringBuilder();
        final BufferedReader br = new BufferedReader(new StringReader(json));
        String line;
        while ((line = br.readLine()) != null) {
            if (!line.strip().startsWith(("//"))) out.append(line).append('\n');
        }
        return out.toString();
    }

    // ===================================================================================
    // Gson object model

    public static class HighspeedConfigGson {
        List<HighspeedBlockConfigGson> blocks;
        Boolean isSpeedometerEnabled;
        Boolean isTrueSpeedometerEnabled;
        Boolean isIceBoatsEnabled;
        Integer defaultSpeedLimit;
        Boolean isExperimentalMovementForceEnabled;        ;
    }

    public static class HighspeedBlockConfigGson {
        String blockId;
        Integer speedLimit;
        Integer cartSpeed; // for backward compat
    }
}
