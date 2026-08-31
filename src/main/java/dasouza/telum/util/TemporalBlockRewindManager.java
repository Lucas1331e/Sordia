package dasouza.telum.util;

import dasouza.telum.block.TelumBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class TemporalBlockRewindManager {

    private TemporalBlockRewindManager() {}

    public static boolean isTemporalBlock(BlockState state) {
        if (state == null) return false;
        if (state.is(TelumBlocks.DEEPSLATE_TEMPORAL_POLISHED) ||
            state.is(TelumBlocks.DEEPSLATE_TEMPORAL_TILES) ||
            state.is(TelumBlocks.TEMPORAL_DEEPSLATE_BRICK) ||
            state.is(TelumBlocks.TEMPORAL_BARREL) ||
            state.is(TelumBlocks.SUSPICIOUS_TEMPORAL_SCULK) ||
            state.is(TelumBlocks.SCULK_TEMPORAL_SHRIEKER)) {
            return true;
        }

        String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        return path.contains("temporal");
    }

    public static boolean isTemporalItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Block block = Block.byItem(stack.getItem());
        if (block != Blocks.AIR && isTemporalBlock(block.defaultBlockState())) {
            return true;
        }
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        return path.contains("temporal");
    }

}

