package dasouza.telum.mixin;

import dasouza.telum.component.AssembledToolData;
import dasouza.telum.item.AssembledToolItem;
import dasouza.telum.tool.ToolType;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public abstract class EnchantmentSupportMixin {

    @Shadow
    public abstract Enchantment.EnchantmentDefinition definition();

    @Inject(method = "isSupportedItem", at = @At("HEAD"), cancellable = true)
    private void telum$validateModularEnchantmentSupport(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() instanceof AssembledToolItem) {
            AssembledToolData data = AssembledToolItem.getToolData(stack);
            if (data != null) {
                HolderSet<Item> supported = this.definition().supportedItems();
                TagKey<Item> tagKey = supported.unwrapKey().orElse(null);
                if (tagKey != null) {
                    String path = tagKey.location().getPath();

                    // Sweeping Edge & Sword specific enchantments are ONLY valid on SWORD modular tools
                    if (path.contains("sweeping") || path.contains("sword")) {
                        if (data.toolType() != ToolType.SWORD) {
                            cir.setReturnValue(false);
                        } else {
                            cir.setReturnValue(true);
                        }
                        return;
                    }

                    // Trident specific enchantments (Riptide, Loyalty, Channeling, Impaling) are ONLY valid on TRIDENT modular tools
                    if (path.contains("trident")) {
                        if (data.toolType() != ToolType.TRIDENT) {
                            cir.setReturnValue(false);
                        } else {
                            cir.setReturnValue(true);
                        }
                        return;
                    }

                    // Mining enchantments (Efficiency, Fortune, Silk Touch) are NOT valid on TRIDENT but valid on all other tools
                    if (path.contains("mining") || path.contains("pickaxe") || path.contains("axe") || path.contains("shovel") || path.contains("hoe")) {
                        if (data.toolType() == ToolType.TRIDENT) {
                            cir.setReturnValue(false);
                        } else {
                            cir.setReturnValue(true);
                        }
                        return;
                    }

                    // General durability / enchantable tags
                    if (path.contains("durability") || path.contains("enchantable") || path.contains("vanishable")) {
                        cir.setReturnValue(true);
                    }
                }
            }
        }
    }

}
