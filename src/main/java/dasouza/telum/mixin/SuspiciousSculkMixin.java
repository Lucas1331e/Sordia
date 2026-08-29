package dasouza.telum.mixin;

import dasouza.telum.block.TelumBlocks;
import dasouza.telum.util.SculkBrushingHandler;
import dasouza.telum.util.TemporalSculkZoneManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BrushableBlockEntity.class)
public abstract class SuspiciousSculkMixin extends BlockEntity {

    @Shadow private int brushCount;
    @Shadow private long brushCountResetsAtTick;
    @Shadow private long coolDownEndsAtTick;

    public SuspiciousSculkMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "brush", at = @At("HEAD"), cancellable = true)
    private void telum$onBrush(long gameTime, ServerLevel level, LivingEntity entity, Direction direction, ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        BlockState state = getBlockState();
        if (state.is(TelumBlocks.SUSPICIOUS_TEMPORAL_SCULK)) {
            // Update timestamps to prevent checkReset() from clearing brushCount to 0
            this.coolDownEndsAtTick = gameTime + 10L;
            this.brushCountResetsAtTick = gameTime + 200L;

            this.brushCount++;

            // Visual dusted property stage (0 to 3) updated as brush progress advances
            int stage = Math.min(this.brushCount / 2, 3);
            if (state.hasProperty(BlockStateProperties.DUSTED)) {
                this.level.setBlock(this.worldPosition, state.setValue(BlockStateProperties.DUSTED, stage), 3);
            }

            level.sendParticles(ParticleTypes.SCULK_CHARGE_POP,
                    this.worldPosition.getX() + 0.5,
                    this.worldPosition.getY() + 0.5,
                    this.worldPosition.getZ() + 0.5,
                    4, 0.2, 0.2, 0.2, 0.02);

            // Slower cleaning: Requires 8 brush cycles (~4 seconds of brushing) before completion
            if (this.brushCount < 8) {
                this.level.playSound(null, this.worldPosition, SoundEvents.BRUSH_SAND, SoundSource.BLOCKS, 0.8f, 1.0f);
                cir.setReturnValue(true);
                return;
            }

            // Brush completed!
            ItemStack loot = SculkBrushingHandler.getRandomSculkLoot(level, level.getRandom());

            if (!TemporalSculkZoneManager.bufferItem(level, this.worldPosition, loot)) {
                ItemEntity itemEntity = new ItemEntity(level,
                        this.worldPosition.getX() + 0.5,
                        this.worldPosition.getY() + 0.5,
                        this.worldPosition.getZ() + 0.5,
                        loot);
                level.addFreshEntity(itemEntity);
            }

            // Completion sound (distinct Sculk charge chime & completion sound)
            level.playSound(null, this.worldPosition, SoundEvents.SCULK_BLOCK_CHARGE, SoundSource.BLOCKS, 1.5f, 1.2f);
            level.playSound(null, this.worldPosition, SoundEvents.BRUSH_SAND_COMPLETED, SoundSource.BLOCKS, 1.2f, 1.0f);

            // Revert block to regular sculk
            level.setBlock(this.worldPosition, Blocks.SCULK.defaultBlockState(), 3);

            this.brushCount = 0;
            this.coolDownEndsAtTick = 0L;
            this.brushCountResetsAtTick = 0L;
            cir.setReturnValue(true);
        }
    }
}
