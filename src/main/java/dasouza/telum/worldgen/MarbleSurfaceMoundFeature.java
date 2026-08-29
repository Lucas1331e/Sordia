package dasouza.telum.worldgen;

import dasouza.telum.block.TelumBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class MarbleSurfaceMoundFeature extends Feature<NoneFeatureConfiguration> {

    public MarbleSurfaceMoundFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        // Surface placement position
        BlockPos surfacePos = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG, origin);
        if (!TelumWorldGen.isSafeChunkPos(origin, surfacePos)) return false;

        BlockState baseState = level.getBlockState(surfacePos.below());

        if (!baseState.isSolidRender() && !baseState.is(Blocks.GRASS_BLOCK) && !baseState.is(Blocks.PODZOL) && !baseState.is(Blocks.SNOW_BLOCK) && !baseState.is(Blocks.DIRT)) {
            return false;
        }

        int radius = 2 + random.nextInt(2);
        int height = 2 + random.nextInt(2);

        boolean placed = false;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                double distSqr = (x * x) + (z * z);
                if (distSqr <= radius * radius) {
                    int colHeight = (int) Math.round((1.0 - (distSqr / (double)(radius * radius))) * height);
                    for (int y = 0; y <= colHeight; y++) {
                        BlockPos p = surfacePos.offset(x, y - 1, z);
                        if (!TelumWorldGen.isSafeChunkPos(origin, p)) continue;

                        if (y == 0 || level.isEmptyBlock(p) || level.getBlockState(p).is(Blocks.SNOW) || level.getBlockState(p).is(Blocks.SHORT_GRASS)) {
                            BlockState blockToPlace = (random.nextFloat() < 0.15f) ?
                                    TelumBlocks.MARMOL_GILDED_BLOCK.defaultBlockState() :
                                    TelumBlocks.MARMOL_BLOCK.defaultBlockState();

                            level.setBlock(p, blockToPlace, 2);
                            placed = true;
                        }
                    }
                }
            }
        }

        return placed;
    }
}
