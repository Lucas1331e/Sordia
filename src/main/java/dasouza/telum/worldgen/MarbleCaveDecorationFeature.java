package dasouza.telum.worldgen;

import dasouza.telum.block.TelumBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Marble Cave Decoration Feature - Adds gilded accents to marble caves.
 * Wall/floor/ceiling coating is handled by MarbleCaveClusterFeature.
 * Lava is handled by vanilla's lake_lava_underground and spring_lava features.
 *
 * This feature only places gilded accents on already-placed marble surfaces.
 */
public class MarbleCaveDecorationFeature extends Feature<NoneFeatureConfiguration> {

    public MarbleCaveDecorationFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        return placeGildedAccents(level, origin, random);
    }

    /**
     * Adds gilded accents to existing marble/blackstone surfaces near air.
     */
    private boolean placeGildedAccents(WorldGenLevel level, BlockPos origin, RandomSource random) {
        boolean placed = false;
        int attempts = 8 + random.nextInt(8);

        for (int i = 0; i < attempts; i++) {
            int rx = random.nextInt(16);
            int rz = random.nextInt(16);
            int ry = random.nextInt(80) - 50;

            BlockPos pos = origin.offset(rx, ry, rz);
            if (!TelumWorldGen.isSafeChunkPos(origin, pos)) continue;

            BlockState state = level.getBlockState(pos);

            // Only accent existing blackstone/marble surfaces next to air
            if (isMarbleFamily(state) && hasAdjacentAir(level, pos, origin)) {
                if (random.nextFloat() < 0.15f) {
                    // Convert some blocks to gilded variants
                    BlockState gilded = state.is(Blocks.BLACKSTONE)
                            ? Blocks.GILDED_BLACKSTONE.defaultBlockState()
                            : TelumBlocks.MARMOL_GILDED_BLOCK.defaultBlockState();
                    level.setBlock(pos, gilded, 2);
                    placed = true;
                }
            }
        }
        return placed;
    }

    private boolean hasAdjacentAir(WorldGenLevel level, BlockPos pos, BlockPos origin) {
        for (Direction dir : Direction.values()) {
            BlockPos rel = pos.relative(dir);
            if (TelumWorldGen.isSafeChunkPos(origin, rel) && level.isEmptyBlock(rel)) {
                return true;
            }
        }
        return false;
    }

    private boolean isMarbleFamily(BlockState state) {
        return state.is(Blocks.BLACKSTONE) || state.is(Blocks.GILDED_BLACKSTONE) ||
               state.is(TelumBlocks.MARMOL_BLOCK) || state.is(TelumBlocks.MARMOL_GILDED_BLOCK);
    }
}
