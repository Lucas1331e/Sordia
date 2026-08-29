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
 * Marble Cave Cluster Feature - Replaces cave surfaces with thick marble layers.
 * Modeled after vanilla's DripstoneClusterFeature / speleothem_cluster.
 *
 * Searches for floor-ceiling pairs within a configurable radius and coats
 * the surrounding stone with marble blocks using the defined proportion.
 */
public class MarbleCaveClusterFeature extends Feature<NoneFeatureConfiguration> {

    private static final int MIN_RADIUS = 3;
    private static final int MAX_RADIUS = 8;
    private static final int MIN_HEIGHT = 2;
    private static final int FLOOR_TO_CEILING_SEARCH_RANGE = 16;
    private static final int MIN_LAYER_THICKNESS = 2;
    private static final int MAX_LAYER_THICKNESS = 4;

    public MarbleCaveClusterFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        // Find a floor-ceiling pair at the origin
        int floorY = findFloor(level, origin, origin);
        if (floorY == Integer.MIN_VALUE) return false;

        int ceilingY = findCeiling(level, origin.atY(floorY + 1), origin);
        if (ceilingY == Integer.MIN_VALUE) return false;

        int caveHeight = ceilingY - floorY - 1;
        if (caveHeight < MIN_HEIGHT) return false;

        int radius = MIN_RADIUS + random.nextInt(MAX_RADIUS - MIN_RADIUS + 1);
        int layerThickness = MIN_LAYER_THICKNESS + random.nextInt(MAX_LAYER_THICKNESS - MIN_LAYER_THICKNESS + 1);
        float density = 0.6f + random.nextFloat() * 0.35f; // 0.6 - 0.95

        boolean placed = false;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                double distSqr = (double)(dx * dx + dz * dz);
                double radiusSqr = (double)(radius * radius);
                if (distSqr > radiusSqr) continue;

                // Density decreases from center
                float distRatio = (float)(distSqr / radiusSqr);
                if (random.nextFloat() < distRatio * (1.0f - density)) continue;

                int colX = origin.getX() + dx;
                int colZ = origin.getZ() + dz;

                BlockPos colPos = new BlockPos(colX, floorY + 2, colZ);
                if (!TelumWorldGen.isSafeChunkPos(origin, colPos)) continue;

                // Find local floor and ceiling for this column
                int localFloorY = findFloor(level, colPos, origin);
                if (localFloorY == Integer.MIN_VALUE) {
                    colPos = new BlockPos(colX, floorY, colZ);
                    if (!TelumWorldGen.isSafeChunkPos(origin, colPos)) continue;
                    localFloorY = findFloor(level, colPos, origin);
                    if (localFloorY == Integer.MIN_VALUE) continue;
                }

                BlockPos ceilCheckPos = new BlockPos(colX, localFloorY + 1, colZ);
                if (!TelumWorldGen.isSafeChunkPos(origin, ceilCheckPos)) continue;
                int localCeilingY = findCeiling(level, ceilCheckPos, origin);
                if (localCeilingY == Integer.MIN_VALUE) continue;

                // Coat the floor (place blocks downward from floor surface)
                for (int depth = 0; depth < layerThickness; depth++) {
                    mutable.set(colX, localFloorY - depth, colZ);
                    if (!TelumWorldGen.isSafeChunkPos(origin, mutable)) continue;
                    BlockState existing = level.getBlockState(mutable);
                    if (canReplace(existing)) {
                        level.setBlock(mutable, getMarbleBlock(random), 2);
                        placed = true;
                    }
                }

                // Coat the ceiling (place blocks upward from ceiling surface)
                for (int depth = 0; depth < layerThickness; depth++) {
                    mutable.set(colX, localCeilingY + depth, colZ);
                    if (!TelumWorldGen.isSafeChunkPos(origin, mutable)) continue;
                    BlockState existing = level.getBlockState(mutable);
                    if (canReplace(existing)) {
                        level.setBlock(mutable, getMarbleBlock(random), 2);
                        placed = true;
                    }
                }

                // Coat exposed walls in the air gap between floor and ceiling
                for (int y = localFloorY + 1; y < localCeilingY; y++) {
                    mutable.set(colX, y, colZ);
                    if (!TelumWorldGen.isSafeChunkPos(origin, mutable)) continue;
                    if (!level.getBlockState(mutable).isAir()) continue;

                    for (Direction dir : Direction.Plane.HORIZONTAL) {
                        BlockPos neighbor = mutable.relative(dir);
                        if (!TelumWorldGen.isSafeChunkPos(origin, neighbor)) continue;
                        BlockState neighborState = level.getBlockState(neighbor);
                        if (canReplace(neighborState)) {
                            // Replace the wall block and go deeper
                            level.setBlock(neighbor, getMarbleBlock(random), 2);
                            placed = true;

                            // Add thickness layers behind the wall
                            BlockPos deeper = neighbor;
                            for (int t = 1; t < layerThickness; t++) {
                                deeper = deeper.relative(dir);
                                if (!TelumWorldGen.isSafeChunkPos(origin, deeper)) break;
                                BlockState deepState = level.getBlockState(deeper);
                                if (canReplace(deepState)) {
                                    level.setBlock(deeper, getMarbleBlock(random), 2);
                                } else {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        return placed;
    }

    /**
     * Searches downward from the given position to find the top of a solid floor.
     */
    private int findFloor(WorldGenLevel level, BlockPos pos, BlockPos origin) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());
        for (int i = 0; i < FLOOR_TO_CEILING_SEARCH_RANGE; i++) {
            if (!TelumWorldGen.isSafeChunkPos(origin, mutable)) break;
            BlockState state = level.getBlockState(mutable);
            BlockState above = level.getBlockState(mutable.above());
            if (state.isSolidRender() && (above.isAir() || above.is(Blocks.WATER))) {
                return mutable.getY();
            }
            mutable.move(Direction.DOWN);
        }
        return Integer.MIN_VALUE;
    }

    /**
     * Searches upward from the given position to find the bottom of a solid ceiling.
     */
    private int findCeiling(WorldGenLevel level, BlockPos pos, BlockPos origin) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());
        for (int i = 0; i < FLOOR_TO_CEILING_SEARCH_RANGE; i++) {
            if (!TelumWorldGen.isSafeChunkPos(origin, mutable)) break;
            BlockState state = level.getBlockState(mutable);
            BlockState below = level.getBlockState(mutable.below());
            if (state.isSolidRender() && (below.isAir() || below.is(Blocks.WATER))) {
                return mutable.getY();
            }
            mutable.move(Direction.UP);
        }
        return Integer.MIN_VALUE;
    }

    private boolean canReplace(BlockState state) {
        return state.is(Blocks.STONE) || state.is(Blocks.DEEPSLATE) ||
               state.is(Blocks.GRANITE) || state.is(Blocks.DIORITE) ||
               state.is(Blocks.ANDESITE) || state.is(Blocks.TUFF) ||
               state.is(Blocks.DIRT) || state.is(Blocks.GRAVEL) ||
               state.is(Blocks.CALCITE) || state.is(Blocks.SANDSTONE) ||
               state.is(Blocks.SMOOTH_BASALT);
    }

    /**
     * Returns a marble block using the defined proportion:
     * 60% Blackstone, 25% Marmol, 2% Gilded Blackstone, 13% Marmol Gilded
     */
    static BlockState getMarbleBlock(RandomSource random) {
        float roll = random.nextFloat();
        if (roll < 0.60f) {
            return Blocks.BLACKSTONE.defaultBlockState();
        } else if (roll < 0.85f) {
            return TelumBlocks.MARMOL_BLOCK.defaultBlockState();
        } else if (roll < 0.87f) {
            return Blocks.GILDED_BLACKSTONE.defaultBlockState();
        } else {
            return TelumBlocks.MARMOL_GILDED_BLOCK.defaultBlockState();
        }
    }
}
