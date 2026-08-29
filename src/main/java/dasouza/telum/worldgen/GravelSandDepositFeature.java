package dasouza.telum.worldgen;

import dasouza.telum.util.GravelSandArchaeologyHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.ArrayList;
import java.util.List;

/**
 * World generation feature that generates or decorates large Gravel and Sand deposits.
 * Places ores and suspicious gravel/sand blocks with contextual loot tables.
 */
public class GravelSandDepositFeature extends Feature<NoneFeatureConfiguration> {

    public GravelSandDepositFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        // Perform multiple attempts in the chunk region
        boolean placedAny = false;
        for (int i = 0; i < 4; i++) {
            int rx = random.nextInt(16);
            int rz = random.nextInt(16);
            int ry = level.getMinY() + random.nextInt(level.getHeight() - 16);
            BlockPos seedPos = origin.offset(rx, ry, rz);

            if (tryDecorateDeposit(level, seedPos, random)) {
                placedAny = true;
            }
        }
        return placedAny;
    }

    private boolean tryDecorateDeposit(WorldGenLevel level, BlockPos center, RandomSource random) {
        BlockState centerState = level.getBlockState(center);
        boolean isGravel = centerState.is(Blocks.GRAVEL);
        boolean isSand = centerState.is(Blocks.SAND) || centerState.is(Blocks.RED_SAND);

        if (!isGravel && !isSand) {
            return false;
        }

        int radiusX = 2 + random.nextInt(3);
        int radiusY = 2 + random.nextInt(2);
        int radiusZ = 2 + random.nextInt(3);

        List<BlockPos> depositPositions = new ArrayList<>();
        Block targetMaterial = isGravel ? Blocks.GRAVEL : (centerState.is(Blocks.RED_SAND) ? Blocks.RED_SAND : Blocks.SAND);

        for (int x = -radiusX; x <= radiusX; x++) {
            for (int y = -radiusY; y <= radiusY; y++) {
                for (int z = -radiusZ; z <= radiusZ; z++) {
                    double dist = (x * x) / (double)(radiusX * radiusX) +
                                  (y * y) / (double)(radiusY * radiusY) +
                                  (z * z) / (double)(radiusZ * radiusZ);
                    if (dist <= 1.0) {
                        BlockPos pos = center.offset(x, y, z);
                        BlockState state = level.getBlockState(pos);
                        if (state.is(targetMaterial)) {
                            depositPositions.add(pos);
                        }
                    }
                }
            }
        }

        // Only decorate if it's a large deposit (>= 6 blocks)
        if (depositPositions.size() < 6) {
            return false;
        }

        Block suspiciousBlock = isGravel ? Blocks.SUSPICIOUS_GRAVEL : Blocks.SUSPICIOUS_SAND;

        for (BlockPos pos : depositPositions) {
            float roll = random.nextFloat();
            if (roll < 0.20f) {
                // 20% Ore replacement
                BlockState oreState = getRandomOreState(pos.getY(), isGravel, random);
                level.setBlock(pos, oreState, 2);
            } else if (roll < 0.45f) {
                // 25% Suspicious Block replacement
                level.setBlock(pos, suspiciousBlock.defaultBlockState(), 2);
                if (level.getBlockEntity(pos) instanceof BrushableBlockEntity brushable) {
                    ResourceKey<LootTable> lootKey = GravelSandArchaeologyHandler.getContextualLootTable(level.getLevel(), pos);
                    brushable.setLootTable(lootKey, random.nextLong());
                }
            }
        }

        return true;
    }

    private BlockState getRandomOreState(int yPos, boolean isGravel, RandomSource random) {
        boolean deep = yPos < 0;
        float roll = random.nextFloat();

        if (deep) {
            if (roll < 0.35f) return Blocks.DEEPSLATE_IRON_ORE.defaultBlockState();
            if (roll < 0.65f) return Blocks.DEEPSLATE_COPPER_ORE.defaultBlockState();
            if (roll < 0.82f) return Blocks.DEEPSLATE_COAL_ORE.defaultBlockState();
            if (roll < 0.93f) return Blocks.DEEPSLATE_GOLD_ORE.defaultBlockState();
            if (roll < 0.98f) return Blocks.DEEPSLATE_LAPIS_ORE.defaultBlockState();
            return Blocks.DEEPSLATE_DIAMOND_ORE.defaultBlockState();
        } else {
            if (roll < 0.40f) return Blocks.IRON_ORE.defaultBlockState();
            if (roll < 0.70f) return Blocks.COPPER_ORE.defaultBlockState();
            if (roll < 0.88f) return Blocks.COAL_ORE.defaultBlockState();
            if (roll < 0.96f) return Blocks.GOLD_ORE.defaultBlockState();
            return Blocks.LAPIS_ORE.defaultBlockState();
        }
    }
}
