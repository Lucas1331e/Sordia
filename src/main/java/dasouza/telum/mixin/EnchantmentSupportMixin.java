package dasouza.telum.mixin;

import dasouza.telum.item.AssembledToolItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public abstract class EnchantmentSupportMixin {

    @Inject(method = "isSupportedItem", at = @At("HEAD"), cancellable = true)
    private void telum$validateModularEnchantmentSupport(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() instanceof AssembledToolItem) {
            cir.setReturnValue(false);
        }
    }
}
