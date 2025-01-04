package net.pcal.highspeed;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import net.minecraft.resources.ResourceLocation;
import net.pcal.highspeed.HighspeedConfig.PerBlockConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

class HighspeedConfigParser {

    static HighspeedConfig parse(final InputStream in) throws IOException {
        final String rawJson = stripComments(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        final Gson gson = new Gson();
        final HighspeedConfigGson configGson = gson.fromJson(rawJson, HighspeedConfigGson.class);
        final ImmutableMap.Builder<ResourceLocation, PerBlockConfig> pbcs = ImmutableMap.builder();
        configGson.blocks.forEach(bcg -> pbcs.put(
                ResourceLocation.parse(requireNonNull(bcg.blockId, "blockId is required")), createPerBlockConfig(bcg)));

        final PerBlockConfig defaultConfig;
        if (configGson.dflt != null) {
            defaultConfig = createPerBlockConfig(configGson.dflt);
        } else if (configGson.defaultSpeedLimit != null) {
            // legacy support
            defaultConfig = new PerBlockConfig(configGson.defaultSpeedLimit, null, null, null, null, null, null, null);
        } else {
            defaultConfig = null;
        }

        // adjust logging to configured level
        return new HighspeedConfig(
                defaultConfig,
                pbcs.build(),
                requireNonNullElse(configGson.isSpeedometerEnabled,true),
                requireNonNullElse(configGson.isTrueSpeedometerEnabled, false),
                requireNonNullElse(configGson.isIceBoatsEnabled, false),
                requireNonNullElse(configGson.isExperimentalMovementForceEnabled, false)
        );
    }

    private static PerBlockConfig createPerBlockConfig(PerBlockConfigGson blockGson) {
        return new PerBlockConfig(
                blockGson.maxSpeed,
                blockGson.boostAmount1,
                blockGson.boostAmount2,
                blockGson.boostThreshold,
                blockGson.haltThreshold,
                blockGson.haltScale,
                blockGson.slowdownFactorOccupied,
                blockGson.slowdownFactorEmpty
        );
    }


    // ===================================================================================
    // Private

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
        @SerializedName(value = "default")
        PerBlockConfigGson dflt;
        List<PerBlockConfigGson> blocks;
        Boolean isSpeedometerEnabled;
        Boolean isTrueSpeedometerEnabled;
        Boolean isIceBoatsEnabled;
        Integer defaultSpeedLimit;
        Boolean isExperimentalMovementForceEnabled;        ;
    }

    public static class PerBlockConfigGson {
        String blockId;
        @SerializedName(value = "maxSpeed", alternate = {"cartSpeed", "speedLimit"}) // alternates for backwards compat
        Integer maxSpeed;
        Double boostAmount1;
        Double boostAmount2;
        Double boostThreshold;
        Double haltThreshold;
        Double haltScale;
        Double slowdownFactorOccupied;
        Double slowdownFactorEmpty;
    }
}
