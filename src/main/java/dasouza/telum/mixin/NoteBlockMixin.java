package dasouza.telum.mixin;

import dasouza.telum.block.TelumBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import net.minecraft.world.level.redstone.Orientation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoteBlock.class)
public class NoteBlockMixin {

    @Inject(method = "updateShape", at = @At("RETURN"))
    private void telum$notifyNoteComparatorOnShapeUpdate(BlockState state, LevelReader levelReader, ScheduledTickAccess tickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random, CallbackInfoReturnable<BlockState> cir) {
        if (levelReader instanceof Level level && !level.isClientSide()) {
            notifyAdjacentComparators(level, pos, state);
        }
    }

    @Inject(method = "neighborChanged", at = @At("RETURN"))
    private void telum$notifyNoteComparatorOnNeighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, Orientation orientation, boolean movedByPiston, CallbackInfo ci) {
        if (!level.isClientSide()) {
            notifyAdjacentComparators(level, pos, state);
        }
    }

    private void notifyAdjacentComparators(Level level, BlockPos pos, BlockState state) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos compPos = pos.relative(dir);
            BlockState compState = level.getBlockState(compPos);
            if (compState.is(TelumBlocks.NOTE_COMPARATOR)) {
                Orientation orientation = ExperimentalRedstoneUtils.initialOrientation(level, dir.getOpposite(), Direction.UP);
                level.neighborChanged(compPos, state.getBlock(), orientation);
            }
        }
    }
}
