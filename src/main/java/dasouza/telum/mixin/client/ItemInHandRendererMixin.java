package dasouza.telum.mixin.client;

import dasouza.telum.item.AssembledToolItem;
import dasouza.telum.item.ToolPartItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {

    @Shadow private ItemStack mainHandItem;
    @Shadow private ItemStack offHandItem;

    @Inject(method = "tick", at = @At("HEAD"), require = 0)
    private void telum$preventReequipAnimationOnComponentUpdate(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            ItemStack currentMain = mc.player.getMainHandItem();
            if (this.mainHandItem != null && !this.mainHandItem.isEmpty() && currentMain != null && !currentMain.isEmpty()) {
                if (this.mainHandItem.getItem() instanceof AssembledToolItem || this.mainHandItem.getItem() instanceof ToolPartItem) {
                    if (this.mainHandItem.is(currentMain.getItem())) {
                        this.mainHandItem = currentMain.copy();
                    }
                }
            }

            ItemStack currentOff = mc.player.getOffhandItem();
            if (this.offHandItem != null && !this.offHandItem.isEmpty() && currentOff != null && !currentOff.isEmpty()) {
                if (this.offHandItem.getItem() instanceof AssembledToolItem || this.offHandItem.getItem() instanceof ToolPartItem) {
                    if (this.offHandItem.is(currentOff.getItem())) {
                        this.offHandItem = currentOff.copy();
                    }
                }
            }
        }
    }
}

