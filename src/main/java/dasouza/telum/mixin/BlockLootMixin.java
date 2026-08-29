package dasouza.telum.mixin;

import dasouza.telum.component.AssembledToolData;
import dasouza.telum.item.AssembledToolItem;
import dasouza.telum.tool.PartMaterial;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Block.class)
public abstract class BlockLootMixin {

    @ModifyVariable(
            method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemInstance;)Ljava/util/List;",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private static ItemInstance telum$injectModularToolLootEnchantments(ItemInstance tool, BlockState state, ServerLevel level, BlockPos pos, BlockEntity blockEntity, Entity entity) {
        if (tool instanceof ItemStack itemStack && itemStack.getItem() instanceof AssembledToolItem) {
            AssembledToolData data = AssembledToolItem.getToolData(itemStack);
            if (data != null) {
                int amethystLvl = data.getMaterialLevel(PartMaterial.AMETHYST);
                int emeraldLvl = data.getMaterialLevel(PartMaterial.EMERALD);

                if (amethystLvl >= 1 || emeraldLvl >= 1) {
                    var registryOpt = level.registryAccess().lookup(Registries.ENCHANTMENT);
                    if (registryOpt.isPresent()) {
                        ItemStack toolCopy = itemStack.copy();
                        ItemEnchantments existing = toolCopy.getOrDefault(net.minecraft.core.component.DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
                        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(existing);

                        if (amethystLvl >= 1) {
                            var silkOpt = registryOpt.get().get(Enchantments.SILK_TOUCH);
                            silkOpt.ifPresent(holder -> mutable.set(holder, Math.max(1, mutable.getLevel(holder))));
                        }

                        if (emeraldLvl >= 1) {
                            var fortuneOpt = registryOpt.get().get(Enchantments.FORTUNE);
                            fortuneOpt.ifPresent(holder -> mutable.set(holder, Math.max(emeraldLvl, mutable.getLevel(holder))));
                        }

                        toolCopy.set(net.minecraft.core.component.DataComponents.ENCHANTMENTS, mutable.toImmutable());
                        return toolCopy;
                    }
                }
            }
        }
        return tool;
    }
}
