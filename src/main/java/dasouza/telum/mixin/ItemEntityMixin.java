package dasouza.telum.mixin;

import dasouza.telum.block.TelumBlocks;
import dasouza.telum.component.TelumComponents;
import dasouza.telum.component.ToolPartData;
import dasouza.telum.item.TelumItems;
import dasouza.telum.item.ToolPartItem;
import dasouza.telum.tool.PartMaterial;
import dasouza.telum.tool.PartType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {

    @Shadow public abstract ItemStack getItem();
    @Shadow public abstract void setItem(ItemStack stack);

    @Unique private int telum$sulfurSubmergedTicks = 0;

    protected ItemEntityMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Inject(method = "fireImmune", at = @At("HEAD"), cancellable = true)
    private void telum$preventFireDamageForCopperInSulfur(CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack = this.getItem();
        if (!stack.isEmpty() && isCopperOrSulfurPart(stack)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    private void telum$cancelLavaDamageForCopperParts(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack = this.getItem();
        if (!stack.isEmpty() && isCopperOrSulfurPart(stack)) {
            if (source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void telum$tickCopperSulfurTransformation(CallbackInfo ci) {
        if (this.level().isClientSide()) return;

        ItemStack stack = this.getItem();
        if (stack.isEmpty()) return;

        ToolPartData partData = stack.get(TelumComponents.TOOL_PART);
        if (partData == null || partData.material() != PartMaterial.COPPER) {
            telum$sulfurSubmergedTicks = 0;
            return;
        }

        BlockPos pos = this.blockPosition();
        boolean inLava = this.isInLava() || this.level().getFluidState(pos).is(Fluids.LAVA) || this.level().getBlockState(pos).is(Blocks.LAVA);

        if (inLava && isSulfurLake(this.level(), pos)) {
            telum$sulfurSubmergedTicks++;

            if (this.level() instanceof ServerLevel serverLevel) {
                // Bubbling clues every 8 ticks
                if (telum$sulfurSubmergedTicks % 8 == 0) {
                    serverLevel.sendParticles(ParticleTypes.LAVA, this.getX(), this.getY() + 0.2, this.getZ(), 4, 0.1, 0.1, 0.1, 0.02);
                    serverLevel.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY() + 0.3, this.getZ(), 6, 0.15, 0.15, 0.15, 0.03);
                    serverLevel.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY() + 0.2, this.getZ(), 5, 0.1, 0.1, 0.1, 0.02);
                    serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.6f, 1.4f);
                }

                // Transform after 60 ticks (3 seconds)
                if (telum$sulfurSubmergedTicks >= 60) {
                    PartType partType = partData.partType();
                    ToolPartItem sulfurItem = TelumItems.getPartItem(partType, PartMaterial.SULFUR);

                    if (sulfurItem != null) {
                        int count = stack.getCount();
                        ItemStack sulfurStack = new ItemStack(sulfurItem, count);
                        this.setItem(sulfurStack);

                        // Transformation effects
                        serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY() + 0.5, this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
                        serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 15, 0.3, 0.3, 0.3, 0.05);
                        serverLevel.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY() + 0.5, this.getZ(), 20, 0.4, 0.4, 0.4, 0.08);

                        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, 1.0f, 0.8f);
                        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.2f, 1.5f);
                    }

                    telum$sulfurSubmergedTicks = 0;
                }
            }
        } else {
            telum$sulfurSubmergedTicks = 0;
        }
    }

    @Unique
    private static boolean isCopperOrSulfurPart(ItemStack stack) {
        ToolPartData data = stack.get(TelumComponents.TOOL_PART);
        if (data != null) {
            return data.material() == PartMaterial.COPPER || data.material() == PartMaterial.SULFUR;
        }
        return false;
    }

    @Unique private static final ResourceKey<net.minecraft.world.level.biome.Biome> MARBLE_CAVES_KEY =
            ResourceKey.create(Registries.BIOME, dasouza.telum.Telum.id("marble_caves"));

    @Unique
    private static boolean isSulfurLake(Level level, BlockPos pos) {
        var biomeHolder = level.getBiome(pos);
        if (biomeHolder.is(MARBLE_CAVES_KEY)) {
            return true;
        }
        for (BlockPos checkPos : BlockPos.betweenClosed(pos.offset(-2, -2, -2), pos.offset(2, 2, 2))) {
            BlockState state = level.getBlockState(checkPos);
            if (state.is(TelumBlocks.MARMOL_BLOCK) || state.is(TelumBlocks.MARMOL_GILDED_BLOCK) || state.is(TelumBlocks.MARMOL_BRICKS)) {
                return true;
            }
        }
        return false;
    }
}
