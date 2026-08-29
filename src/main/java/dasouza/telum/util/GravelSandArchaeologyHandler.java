package dasouza.telum.util;

import dasouza.telum.Telum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.Locale;

public final class GravelSandArchaeologyHandler {

    public static final ResourceKey<LootTable> UNDERGROUND_LOOT_TABLE =
            ResourceKey.create(Registries.LOOT_TABLE, Telum.id("archaeology/underground"));
    public static final ResourceKey<LootTable> BEACH_LOOT_TABLE =
            ResourceKey.create(Registries.LOOT_TABLE, Telum.id("archaeology/surface_beach"));
    public static final ResourceKey<LootTable> DESERT_LOOT_TABLE =
            ResourceKey.create(Registries.LOOT_TABLE, Telum.id("archaeology/surface_desert"));
    public static final ResourceKey<LootTable> MOUNTAIN_LOOT_TABLE =
            ResourceKey.create(Registries.LOOT_TABLE, Telum.id("archaeology/surface_mountain"));
    public static final ResourceKey<LootTable> GENERIC_SURFACE_LOOT_TABLE =
            ResourceKey.create(Registries.LOOT_TABLE, Telum.id("archaeology/surface_generic"));
    public static final ResourceKey<LootTable> MARBLE_CAVE_LOOT_TABLE =
            ResourceKey.create(Registries.LOOT_TABLE, Telum.id("archaeology/marble_cave"));

    private GravelSandArchaeologyHandler() {}

    public static ResourceKey<LootTable> getContextualLootTable(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return UNDERGROUND_LOOT_TABLE;
        }

        Holder<Biome> biomeHolder = level.getBiome(pos);
        if (isBiomeNameMatch(biomeHolder, "marble_caves")) {
            return MARBLE_CAVE_LOOT_TABLE;
        }

        boolean isUnderground = !level.canSeeSky(pos) || pos.getY() < (level.getSeaLevel() - 10);
        if (isUnderground) {
            return UNDERGROUND_LOOT_TABLE;
        }

        if (biomeHolder.is(BiomeTags.IS_BEACH) || biomeHolder.is(BiomeTags.IS_OCEAN) || isBiomeNameMatch(biomeHolder, "beach", "shore", "coast", "ocean")) {
            return BEACH_LOOT_TABLE;
        }
        if (biomeHolder.is(BiomeTags.IS_BADLANDS) || isBiomeNameMatch(biomeHolder, "desert", "badlands", "eroded_badlands")) {
            return DESERT_LOOT_TABLE;
        }
        if (biomeHolder.is(BiomeTags.IS_MOUNTAIN) || biomeHolder.is(BiomeTags.IS_HILL) || isBiomeNameMatch(biomeHolder, "mountain", "peak", "slope", "hills", "jagged", "frozen_peaks", "stony_peaks", "windswept")) {
            return MOUNTAIN_LOOT_TABLE;
        }

        return GENERIC_SURFACE_LOOT_TABLE;
    }

    private static boolean isBiomeNameMatch(Holder<Biome> biomeHolder, String... keywords) {
        var key = biomeHolder.unwrapKey();
        if (key.isPresent()) {
            String path = key.get().identifier().getPath().toLowerCase(Locale.ROOT);
            for (String keyword : keywords) {
                if (path.contains(keyword)) {
                    return true;
                }
            }
        }
        return false;
    }
}
