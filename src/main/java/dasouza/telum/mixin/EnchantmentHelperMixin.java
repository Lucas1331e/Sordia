package dasouza.telum.mixin;

import dasouza.telum.component.AssembledToolData;
import dasouza.telum.item.AssembledToolItem;
import dasouza.telum.tool.PartMaterial;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {

    @Inject(method = "getItemEnchantmentLevel", at = @At("RETURN"), cancellable = true)
    private static void telum$injectModularEnchantmentLevels(Holder<Enchantment> enchantment, ItemInstance stack, CallbackInfoReturnable<Integer> cir) {
        if (stack instanceof ItemStack itemStack && itemStack.getItem() instanceof AssembledToolItem) {
            AssembledToolData data = AssembledToolItem.getToolData(itemStack);
            if (data != null) {
                int currentLvl = cir.getReturnValue();

                // Emerald Material Ability: Built-in Fortune (Abundancia Mercantil)
                if (enchantment.is(Enchantments.FORTUNE)) {
                    int emeraldLvl = data.getMaterialLevel(PartMaterial.EMERALD);
                    if (emeraldLvl > currentLvl) {
                        cir.setReturnValue(emeraldLvl);
                    }
                }

                // Amethyst Material Ability: Built-in Silk Touch (Toque de Resonancia)
                if (enchantment.is(Enchantments.SILK_TOUCH)) {
                    int amethystLvl = data.getMaterialLevel(PartMaterial.AMETHYST);
                    if (amethystLvl >= 1 && currentLvl < 1) {
                        cir.setReturnValue(1);
                    }
                }
            }
        }
    }
}
