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
                    if ((path.contains("sweeping") || path.contains("sword")) && data.toolType() != ToolType.SWORD) {
                        cir.setReturnValue(false);
                        return;
                    }

                    // Trident specific enchantments (Riptide, Loyalty, Channeling, Impaling) are ONLY valid on TRIDENT modular tools
                    if (path.contains("trident") && data.toolType() != ToolType.TRIDENT) {
                        cir.setReturnValue(false);
                        return;
                    }

                    // Mining enchantments (Efficiency, Fortune, Silk Touch) are NOT valid on TRIDENT
                    if (path.contains("mining") && data.toolType() == ToolType.TRIDENT) {
                        cir.setReturnValue(false);
                    }
                }
            }
        }
    }
}
