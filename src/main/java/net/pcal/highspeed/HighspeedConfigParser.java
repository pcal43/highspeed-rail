package net.pcal.highspeed;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import net.minecraft.resources.ResourceLocation;
import net.pcal.highspeed.HighspeedConfig.PerBlockConfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

class HighspeedConfigParser {

    static HighspeedConfig parse(final InputStream in) throws IOException {
        final String rawJson = (new String(in.readAllBytes(), StandardCharsets.UTF_8));
        class TypoCatchingJsonReader extends JsonReader {
            public TypoCatchingJsonReader(StringReader in) {
                super(in);
                super.setLenient(true);
            }

            @Override
            public void skipValue()  {
                // GSon calls this to silently ignore json keys that don't bind to anything.  People then get
                // confused about why their configuration isn't fully working.  So here we just fail loudly instead.
                // Note we don't throw IOException because GSon tries to handle that in a ways that obscures the message.
                throw new RuntimeException("Unexpected configuration names at: "+this.getPath());
            }
        }
        final HighspeedConfigGson configGson =
                new Gson().fromJson(new TypoCatchingJsonReader(new StringReader(rawJson)), TypeToken.get(HighspeedConfigGson.class));

        // load the default config
        final PerBlockConfig defaultConfig;
        {
            if (configGson.defaults != null) {
                defaultConfig = createPerBlockConfig(configGson.defaults);
            } else if (configGson.defaultSpeedLimit != null) { // legacy support
                defaultConfig = new PerBlockConfig(configGson.defaultSpeedLimit, null, null, null, null, null, null, null);
            } else {
                defaultConfig = null;
            }
        }

        // load the per-block configs
        final ImmutableMap<ResourceLocation, PerBlockConfig> perBlockConfigs;
        {
            if (configGson.blocks != null) {
                final ImmutableMap.Builder<ResourceLocation, PerBlockConfig> pbcs = ImmutableMap.builder();
                configGson.blocks.forEach(bcg -> {
                            final Collection<String> blockIds;
                            if (bcg.blockIds != null) {
                                blockIds = bcg.blockIds;
                            } else if (bcg.blockId != null) { // legacy config support
                                blockIds = Set.of(bcg.blockId);
                            } else {
                                throw new RuntimeException("blockIds must be set in 'blocks' configurations");
                            }
                            for (String blockId : blockIds) {
                                pbcs.put(ResourceLocation.parse(requireNonNull(blockId, "blockIds must not be null")),
                                         mergeConfigs(defaultConfig, createPerBlockConfig(bcg)));
                            }
                        });
                perBlockConfigs = pbcs.build();
            } else {
                perBlockConfigs = null;
            }
        }

        // assemble the final config object
        return new HighspeedConfig(
                defaultConfig,
                perBlockConfigs,
                requireNonNullElse(configGson.isSpeedometerEnabled,true),
                requireNonNullElse(configGson.isTrueSpeedometerEnabled, false),
                requireNonNullElse(configGson.isIceBoatsEnabled, false),
                requireNonNullElse(configGson.isExperimentalMovementForceEnabled, false)
        );
    }


    // ===================================================================================
    // Private

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

    private static PerBlockConfig mergeConfigs(PerBlockConfig base, PerBlockConfig overrides) {
        if (base == null) return overrides;
        return new PerBlockConfig(
                elvis(overrides.maxSpeed(), base.maxSpeed()),
                elvis(overrides.boostAmount1(), base.boostAmount1()),
                elvis(overrides.boostAmount2(), base.boostAmount2()),
                elvis(overrides.boostThreshold(), base.boostThreshold()),
                elvis(overrides.haltThreshold(), base.haltThreshold()),
                elvis(overrides.haltScale(), base.haltScale()),
                elvis(overrides.slowdownFactorOccupied(), base.slowdownFactorOccupied()),
                elvis(overrides.slowdownFactorEmpty(), base.slowdownFactorEmpty())
        );
    }

    private static <T> T elvis(T first, T second) {
        return first != null ? first : second;
    }

    // ===================================================================================
    // Gson object model

    public static class HighspeedConfigGson {
        PerBlockConfigGson defaults;
        List<PerBlockConfigGson> blocks;
        Boolean isSpeedometerEnabled;
        Boolean isTrueSpeedometerEnabled;
        Boolean isIceBoatsEnabled;
        Boolean isExperimentalMovementForceEnabled;

        @Deprecated // supports older configs, use defaults/maxSpeed going forward
        Integer defaultSpeedLimit;
    }

    public static class PerBlockConfigGson {
        List<String> blockIds;
        @SerializedName(value = "maxSpeed", alternate = {"cartSpeed", "speedLimit"}) // alternates for backwards compat
        Integer maxSpeed;
        Double boostAmount1;
        Double boostAmount2;
        Double boostThreshold;
        Double haltThreshold;
        Double haltScale;
        Double slowdownFactorOccupied;
        Double slowdownFactorEmpty;

        @Deprecated // supports older configs, use blockIds going forward
        String blockId;
    }
}
