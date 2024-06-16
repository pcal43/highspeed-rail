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

class HighspeedConfigParser {

    static HighspeedConfig parse(final InputStream in) throws IOException {
        final List<HighspeedBlockConfig> blocks = new ArrayList<>();
        final String rawJson = stripComments(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        final Gson gson = new Gson();
        final HighspeedConfigGson configGson = gson.fromJson(rawJson, HighspeedConfigGson.class);
        for (HighspeedBlockConfigGson blockGson : configGson.blocks) {
            HighspeedBlockConfig bc = new HighspeedBlockConfig(
                    ResourceLocation.parse(requireNonNull(blockGson.blockId, "blockId is required")),
                    requireNonNull(blockGson.cartSpeed)
            );
            blocks.add(bc);
        }
        // adjust logging to configured level
        return new HighspeedConfig(
                Collections.unmodifiableList(blocks),
                requireNonNull(configGson.isSpeedometerEnabled, "isSpeedometerEnabled must be set"),
                requireNonNull(configGson.isTrueSpeedometerEnabled, "isTrueSpeedometerEnabled must be set"),
                requireNonNull(configGson.isIceBoatsEnabled, "isIceBoatsEnabled must be set")
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
    }

    public static class HighspeedBlockConfigGson {
        String blockId;
        Integer cartSpeed;
    }
}
