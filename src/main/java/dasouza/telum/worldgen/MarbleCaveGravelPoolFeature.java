package dasouza.telum.worldgen;

import dasouza.telum.util.GravelSandArchaeologyHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class MarbleCaveGravelPoolFeature extends Feature<NoneFeatureConfiguration> {

    public MarbleCaveGravelPoolFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        // Search downward from origin to find a cave floor
        BlockPos floorPos = findCaveFloor(level, origin, origin);
        if (floorPos == null) return false;

        return placeGravelPool(level, origin, floorPos, random);
    }

    /**
     * Searches downward from the origin to find a solid block with air above (cave floor).
     */
    private BlockPos findCaveFloor(WorldGenLevel level, BlockPos pos, BlockPos origin) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());
        for (int i = 0; i < 20; i++) {
            if (!TelumWorldGen.isSafeChunkPos(origin, mutable)) break;
            BlockState current = level.getBlockState(mutable);
            BlockState above = level.getBlockState(mutable.above());
            if (current.isSolidRender() && (above.isAir() || above.is(Blocks.WATER))) {
                return mutable.immutable();
            }
            mutable.move(Direction.DOWN);
        }
        // Also try searching upward
        mutable.set(pos.getX(), pos.getY(), pos.getZ());
        for (int i = 0; i < 20; i++) {
            if (!TelumWorldGen.isSafeChunkPos(origin, mutable)) break;
            BlockState current = level.getBlockState(mutable);
            BlockState above = level.getBlockState(mutable.above());
            if (current.isSolidRender() && (above.isAir() || above.is(Blocks.WATER))) {
                return mutable.immutable();
            }
            mutable.move(Direction.UP);
        }
        return null;
    }

    private boolean placeGravelPool(WorldGenLevel level, BlockPos origin, BlockPos center, RandomSource random) {
        int radiusX = 2 + random.nextInt(3);
        int radiusZ = 2 + random.nextInt(3);

        int count = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = -radiusX; x <= radiusX; x++) {
            for (int z = -radiusZ; z <= radiusZ; z++) {
                if ((x * x) + (z * z) <= (radiusX * radiusZ)) {
                    pos.set(center.getX() + x, center.getY(), center.getZ() + z);

                    if (!TelumWorldGen.isSafeChunkPos(origin, pos)) continue;

                    // Find actual floor at this column
                    BlockPos surfacePos = findLocalFloor(level, pos, origin);
                    if (surfacePos == null) continue;

                    BlockState current = level.getBlockState(surfacePos);
                    if (!current.isSolidRender()) continue;

                    BlockState above = level.getBlockState(surfacePos.above());
                    if (!above.isAir() && !above.is(Blocks.WATER)) continue;

                    float roll = random.nextFloat();
                    if (roll < 0.50f) {
                        // 50% Suspicious Gravel with marble cave loot
                        level.setBlock(surfacePos, Blocks.SUSPICIOUS_GRAVEL.defaultBlockState(), 2);
                        if (level.getBlockEntity(surfacePos) instanceof BrushableBlockEntity brushable) {
                            brushable.setLootTable(GravelSandArchaeologyHandler.MARBLE_CAVE_LOOT_TABLE, random.nextLong());
                        }
                        count++;
                    } else if (roll < 0.90f) {
                        // 40% Normal Gravel
                        level.setBlock(surfacePos, Blocks.GRAVEL.defaultBlockState(), 2);
                        count++;
                    }
                }
            }
        }
        return count > 0;
    }

    /**
     * Finds the floor position near the given position by searching up/down a few blocks.
     */
    private BlockPos findLocalFloor(WorldGenLevel level, BlockPos pos, BlockPos origin) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());
        // Search down a few blocks
        for (int i = 0; i < 5; i++) {
            if (!TelumWorldGen.isSafeChunkPos(origin, mutable)) break;
            BlockState state = level.getBlockState(mutable);
            BlockState above = level.getBlockState(mutable.above());
            if (state.isSolidRender() && (above.isAir() || above.is(Blocks.WATER))) {
                return mutable.immutable();
            }
            mutable.move(Direction.DOWN);
        }
        // Search up a few blocks
        mutable.set(pos.getX(), pos.getY(), pos.getZ());
        for (int i = 0; i < 5; i++) {
            if (!TelumWorldGen.isSafeChunkPos(origin, mutable)) break;
            BlockState state = level.getBlockState(mutable);
            BlockState above = level.getBlockState(mutable.above());
            if (state.isSolidRender() && (above.isAir() || above.is(Blocks.WATER))) {
                return mutable.immutable();
            }
            mutable.move(Direction.UP);
        }
        return null;
    }
}
