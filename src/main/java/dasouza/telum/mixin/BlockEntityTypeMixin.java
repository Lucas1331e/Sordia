package dasouza.telum.mixin;

import dasouza.telum.block.TelumBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntityType.class)
public abstract class BlockEntityTypeMixin {

    @Inject(method = "isValid", at = @At("HEAD"), cancellable = true)
    private void telum$isValid(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (state.is(TelumBlocks.SUSPICIOUS_END_STONE)
                || state.is(TelumBlocks.SUSPICIOUS_TEMPORAL_SCULK)
                || state.is(TelumBlocks.SUSPICIOUS_NETHERRACK)
                || state.is(TelumBlocks.SCULK_TEMPORAL_SHRIEKER)) {
            cir.setReturnValue(true);
        }
    }
}
