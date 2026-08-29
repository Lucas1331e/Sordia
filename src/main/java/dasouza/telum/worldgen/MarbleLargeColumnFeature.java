package dasouza.telum.worldgen;

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
 * Marble Large Column Feature - Creates large marble columns from floor to ceiling.
 * Modeled after vanilla's LargeDripstoneFeature.
 *
 * Generates conical stalagmites (floor-up) and stalactites (ceiling-down) that
 * may meet to form full columns. Uses the 4-block marble proportion for all blocks.
 */
public class MarbleLargeColumnFeature extends Feature<NoneFeatureConfiguration> {

    private static final int FLOOR_CEILING_SEARCH = 30;
    private static final int MIN_COLUMN_RADIUS = 1;
    private static final int MAX_COLUMN_RADIUS = 5;

    public MarbleLargeColumnFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        // Find floor and ceiling
        int floorY = findFloor(level, origin, origin);
        if (floorY == Integer.MIN_VALUE) return false;

        int ceilingY = findCeiling(level, new BlockPos(origin.getX(), floorY + 1, origin.getZ()), origin);
        if (ceilingY == Integer.MIN_VALUE) return false;

        int caveHeight = ceilingY - floorY - 1;
        if (caveHeight < 4) return false;

        int columnRadius = MIN_COLUMN_RADIUS + random.nextInt(MAX_COLUMN_RADIUS - MIN_COLUMN_RADIUS + 1);

        // Cap column radius based on cave height ratio (like vanilla's max_column_radius_to_cave_height_ratio = 0.33)
        columnRadius = Math.min(columnRadius, Math.max(1, (int)(caveHeight * 0.33f)));

        float heightScale = 0.4f + random.nextFloat() * 1.6f; // 0.4 to 2.0
        float stalagmiteBluntness = 0.4f + random.nextFloat() * 0.6f; // 0.4 to 1.0
        float stalactiteBluntness = 0.3f + random.nextFloat() * 0.6f; // 0.3 to 0.9

        // Wind offset for slight torsion
        float windX = 0, windZ = 0;
        if (columnRadius >= 2 && random.nextFloat() < 0.5f) {
            float windSpeed = random.nextFloat() * 0.3f;
            float windAngle = random.nextFloat() * (float)(Math.PI * 2);
            windX = (float)(Math.cos(windAngle) * windSpeed);
            windZ = (float)(Math.sin(windAngle) * windSpeed);
        }

        boolean placed = false;

        // Calculate column heights
        int stalagmiteHeight = calculateColumnHeight(caveHeight, heightScale, stalagmiteBluntness, random);
        int stalactiteHeight = calculateColumnHeight(caveHeight, heightScale, stalactiteBluntness, random);

        // Build stalagmite (floor up)
        placed |= buildCone(level, random, origin, origin.getX(), origin.getZ(),
                floorY + 1, stalagmiteHeight, columnRadius,
                true, stalagmiteBluntness, windX, windZ);

        // Build stalactite (ceiling down)
        placed |= buildCone(level, random, origin, origin.getX(), origin.getZ(),
                ceilingY - 1, stalactiteHeight, columnRadius,
                false, stalactiteBluntness, windX, windZ);

        return placed;
    }

    private int calculateColumnHeight(int caveHeight, float heightScale, float bluntness, RandomSource random) {
        float baseHeight = (caveHeight * 0.5f) * heightScale;
        // Bluntness makes columns shorter and wider
        float adjusted = baseHeight * (1.0f - bluntness * 0.3f);
        return Math.max(2, (int) adjusted + random.nextInt(3));
    }

    /**
     * Builds a conical formation (stalagmite growing up, or stalactite growing down).
     */
    private boolean buildCone(WorldGenLevel level, RandomSource random, BlockPos origin,
                              int centerX, int centerZ, int startY, int height,
                              int maxRadius, boolean growUp, float bluntness,
                              float windX, float windZ) {
        boolean placed = false;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int layer = 0; layer < height; layer++) {
            int y = growUp ? startY + layer : startY - layer;
            if (y < level.getMinY() + 1 || y > level.getMaxY() - 1) continue;

            // Calculate radius at this height - linearly decreasing from base to tip
            float progress = (float) layer / (float) height;
            // Bluntness makes the tip wider
            float radiusAtHeight = maxRadius * (1.0f - progress * (1.0f - bluntness * 0.5f));
            int currentRadius = Math.max(0, Math.round(radiusAtHeight));

            // Apply wind offset
            float offsetX = windX * layer;
            float offsetZ = windZ * layer;

            for (int dx = -currentRadius; dx <= currentRadius; dx++) {
                for (int dz = -currentRadius; dz <= currentRadius; dz++) {
                    double distSqr = dx * dx + dz * dz;
                    if (distSqr > (double)(currentRadius * currentRadius) + 0.5) continue;

                    int bx = centerX + dx + Math.round(offsetX);
                    int bz = centerZ + dz + Math.round(offsetZ);
                    mutable.set(bx, y, bz);

                    if (!TelumWorldGen.isSafeChunkPos(origin, mutable)) continue;

                    BlockState existing = level.getBlockState(mutable);
                    if (existing.isAir() || existing.is(Blocks.WATER) || canReplace(existing)) {
                        level.setBlock(mutable, MarbleCaveClusterFeature.getMarbleBlock(random), 2);
                        placed = true;
                    }
                }
            }
        }

        return placed;
    }

    private int findFloor(WorldGenLevel level, BlockPos pos, BlockPos origin) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());
        for (int i = 0; i < FLOOR_CEILING_SEARCH; i++) {
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

    private int findCeiling(WorldGenLevel level, BlockPos pos, BlockPos origin) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());
        for (int i = 0; i < FLOOR_CEILING_SEARCH; i++) {
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
}
