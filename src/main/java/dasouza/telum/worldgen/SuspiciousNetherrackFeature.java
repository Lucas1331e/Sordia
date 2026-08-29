package dasouza.telum.worldgen;

import dasouza.telum.Telum;
import dasouza.telum.block.TelumBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * Custom worldgen feature that rarely places Suspicious Netherrack in veins of 1-3 blocks
 * replacing exposed Netherrack in the Nether dimension.
 */
public class SuspiciousNetherrackFeature extends Feature<NoneFeatureConfiguration> {

    private static final ResourceKey<LootTable> SUSPICIOUS_NETHERRACK_LOOT_TABLE =
            ResourceKey.create(Registries.LOOT_TABLE, Telum.id("archaeology/suspicious_netherrack"));

    public SuspiciousNetherrackFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        // Rare spawn chance per chunk attempt
        if (random.nextFloat() > 0.25f) {
            return false;
        }

        boolean placedAny = false;
        for (int attempt = 0; attempt < 10; attempt++) {
            int rx = random.nextInt(16);
            int rz = random.nextInt(16);
            int ry = 10 + random.nextInt(105);
            BlockPos startPos = origin.offset(rx, ry, rz);

            if (isExposedNetherrack(level, startPos)) {
                int veinSize = 1 + random.nextInt(3); // 1 to 3 blocks
                int placedCount = 0;

                BlockPos.MutableBlockPos current = startPos.mutable();
                for (int i = 0; i < veinSize * 3 && placedCount < veinSize; i++) {
                    if (isExposedNetherrack(level, current)) {
                        level.setBlock(current, TelumBlocks.SUSPICIOUS_NETHERRACK.defaultBlockState(), 2);
                        if (level.getBlockEntity(current) instanceof BrushableBlockEntity brushable) {
                            brushable.setLootTable(SUSPICIOUS_NETHERRACK_LOOT_TABLE, random.nextLong());
                        }
                        placedCount++;
                        placedAny = true;
                    }
                    Direction randomDir = Direction.getRandom(random);
                    current.move(randomDir);
                }

                if (placedAny) {
                    break;
                }
            }
        }

        return placedAny;
    }

    private boolean isExposedNetherrack(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.is(Blocks.NETHERRACK)) {
            return false;
        }

        // Must have at least one adjacent block that is air or non-solid (exposed to cave/atmosphere/fluid)
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            BlockState neighborState = level.getBlockState(neighborPos);
            if (neighborState.isAir() || !neighborState.isRedstoneConductor(level, neighborPos)) {
                return true;
            }
        }
        return false;
    }
}
