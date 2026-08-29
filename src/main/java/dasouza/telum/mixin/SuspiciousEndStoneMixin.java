package dasouza.telum.mixin;

import dasouza.telum.Telum;
import dasouza.telum.block.TelumBlocks;
import dasouza.telum.util.GravelSandArchaeologyHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BrushableBlockEntity.class)
public abstract class SuspiciousEndStoneMixin extends BlockEntity {

    @Shadow
    private ResourceKey<LootTable> lootTable;
    @Shadow
    private ItemStack item;

    private static final ResourceKey<LootTable> TELUM_SUSPICIOUS_END_STONE_LOOT_TABLE =
            ResourceKey.create(Registries.LOOT_TABLE, Telum.id("archaeology/suspicious_end_stone"));
    private static final ResourceKey<LootTable> TELUM_SUSPICIOUS_NETHERRACK_LOOT_TABLE =
            ResourceKey.create(Registries.LOOT_TABLE, Telum.id("archaeology/suspicious_netherrack"));

    public SuspiciousEndStoneMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void telum$onInit(BlockPos pos, BlockState state, CallbackInfo ci) {
        if (this.lootTable == null && (this.item == null || this.item.isEmpty())) {
            if (state.is(TelumBlocks.SUSPICIOUS_END_STONE) || state.is(TelumBlocks.SUSPICIOUS_TEMPORAL_SCULK)) {
                this.lootTable = TELUM_SUSPICIOUS_END_STONE_LOOT_TABLE;
            } else if (state.is(TelumBlocks.SUSPICIOUS_NETHERRACK)) {
                this.lootTable = TELUM_SUSPICIOUS_NETHERRACK_LOOT_TABLE;
            }
        }
    }

    @Inject(method = "brush", at = @At("HEAD"))
    private void telum$onBrushHead(long gameTime, ServerLevel level, LivingEntity entity, Direction direction, ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        if (this.lootTable == null && (this.item == null || this.item.isEmpty())) {
            BlockState state = getBlockState();
            if (state.is(Blocks.SUSPICIOUS_GRAVEL) || state.is(Blocks.SUSPICIOUS_SAND)) {
                this.lootTable = GravelSandArchaeologyHandler.getContextualLootTable(level, this.worldPosition);
            }
        }
    }
}


