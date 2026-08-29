package dasouza.telum.worldgen;

import dasouza.telum.Telum;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public final class TelumWorldGen {

    public static final Feature<NoneFeatureConfiguration> GRAVEL_SAND_DEPOSIT_FEATURE = new GravelSandDepositFeature();
    public static final ResourceKey<PlacedFeature> GRAVEL_SAND_DEPOSIT_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, Telum.id("gravel_sand_deposit"));

    public static final Feature<NoneFeatureConfiguration> SUSPICIOUS_NETHERRACK_FEATURE = new SuspiciousNetherrackFeature();
    public static final ResourceKey<PlacedFeature> SUSPICIOUS_NETHERRACK_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, Telum.id("suspicious_netherrack"));

    public static final Feature<NoneFeatureConfiguration> MARBLE_CAVE_CLUSTER_FEATURE = new MarbleCaveClusterFeature();
    public static final ResourceKey<PlacedFeature> MARBLE_CAVE_CLUSTER_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, Telum.id("marble_cave_cluster"));

    public static final Feature<NoneFeatureConfiguration> MARBLE_LARGE_COLUMN_FEATURE = new MarbleLargeColumnFeature();
    public static final ResourceKey<PlacedFeature> MARBLE_LARGE_COLUMN_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, Telum.id("marble_large_column"));

    public static final Feature<NoneFeatureConfiguration> MARBLE_CAVE_DECORATION_FEATURE = new MarbleCaveDecorationFeature();
    public static final ResourceKey<PlacedFeature> MARBLE_CAVE_DECORATION_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, Telum.id("marble_cave_decoration"));

    public static final Feature<NoneFeatureConfiguration> MARBLE_CAVE_GRAVEL_POOL_FEATURE = new MarbleCaveGravelPoolFeature();
    public static final ResourceKey<PlacedFeature> MARBLE_CAVE_GRAVEL_POOL_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, Telum.id("marble_cave_gravel_pool"));

    public static final Feature<NoneFeatureConfiguration> MARBLE_SURFACE_MOUND_FEATURE = new MarbleSurfaceMoundFeature();
    public static final ResourceKey<PlacedFeature> MARBLE_SURFACE_MOUND_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, Telum.id("marble_surface_mound"));

    private TelumWorldGen() {}

    /**
     * Checks if targetPos is within the safe 3x3 chunk radius of origin.
     * Prevents "Detected unsafe terrain read during worldgen" and "Detected setBlock in a far chunk" errors.
     */
    public static boolean isSafeChunkPos(BlockPos origin, BlockPos targetPos) {
        return Math.abs((targetPos.getX() >> 4) - (origin.getX() >> 4)) <= 1 &&
               Math.abs((targetPos.getZ() >> 4) - (origin.getZ() >> 4)) <= 1;
    }

    public static void initialize() {
        Telum.LOGGER.info("Registering Telum WorldGen Features");

        Registry.register(BuiltInRegistries.FEATURE, Telum.id("gravel_sand_deposit"), GRAVEL_SAND_DEPOSIT_FEATURE);
        Registry.register(BuiltInRegistries.FEATURE, Telum.id("suspicious_netherrack"), SUSPICIOUS_NETHERRACK_FEATURE);
        Registry.register(BuiltInRegistries.FEATURE, Telum.id("marble_cave_cluster"), MARBLE_CAVE_CLUSTER_FEATURE);
        Registry.register(BuiltInRegistries.FEATURE, Telum.id("marble_large_column"), MARBLE_LARGE_COLUMN_FEATURE);
        Registry.register(BuiltInRegistries.FEATURE, Telum.id("marble_cave_decoration"), MARBLE_CAVE_DECORATION_FEATURE);
        Registry.register(BuiltInRegistries.FEATURE, Telum.id("marble_cave_gravel_pool"), MARBLE_CAVE_GRAVEL_POOL_FEATURE);
        Registry.register(BuiltInRegistries.FEATURE, Telum.id("marble_surface_mound"), MARBLE_SURFACE_MOUND_FEATURE);

        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Decoration.UNDERGROUND_ORES,
                GRAVEL_SAND_DEPOSIT_PLACED
        );

        BiomeModifications.addFeature(
                BiomeSelectors.foundInTheNether(),
                GenerationStep.Decoration.UNDERGROUND_DECORATION,
                SUSPICIOUS_NETHERRACK_PLACED
        );

        // Surface mounds placed under Taiga and Mega Taiga biomes
        BiomeModifications.addFeature(
                BiomeSelectors.includeByKey(
                        net.minecraft.world.level.biome.Biomes.TAIGA,
                        net.minecraft.world.level.biome.Biomes.OLD_GROWTH_PINE_TAIGA,
                        net.minecraft.world.level.biome.Biomes.OLD_GROWTH_SPRUCE_TAIGA,
                        net.minecraft.world.level.biome.Biomes.SNOWY_TAIGA
                ),
                GenerationStep.Decoration.SURFACE_STRUCTURES,
                MARBLE_SURFACE_MOUND_PLACED
        );
    }
}
