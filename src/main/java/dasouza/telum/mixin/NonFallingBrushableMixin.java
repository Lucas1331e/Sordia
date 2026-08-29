package dasouza.telum.mixin;

import dasouza.telum.block.TelumBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BrushableBlock.class)
public abstract class NonFallingBrushableMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void telum$noGravityTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (state.is(TelumBlocks.SUSPICIOUS_NETHERRACK) || state.is(TelumBlocks.SUSPICIOUS_END_STONE) || state.is(TelumBlocks.SUSPICIOUS_TEMPORAL_SCULK)) {
            ci.cancel();
        }
    }

    @Inject(method = "onPlace", at = @At("HEAD"), cancellable = true)
    private void telum$noGravityOnPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston, CallbackInfo ci) {
        if (state.is(TelumBlocks.SUSPICIOUS_NETHERRACK) || state.is(TelumBlocks.SUSPICIOUS_END_STONE) || state.is(TelumBlocks.SUSPICIOUS_TEMPORAL_SCULK)) {
            ci.cancel();
        }
    }

    @Inject(method = "updateShape", at = @At("HEAD"), cancellable = true)
    private void telum$noGravityUpdateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random, CallbackInfoReturnable<BlockState> cir) {
        if (state.is(TelumBlocks.SUSPICIOUS_NETHERRACK) || state.is(TelumBlocks.SUSPICIOUS_END_STONE) || state.is(TelumBlocks.SUSPICIOUS_TEMPORAL_SCULK)) {
            cir.setReturnValue(state);
        }
    }
}
