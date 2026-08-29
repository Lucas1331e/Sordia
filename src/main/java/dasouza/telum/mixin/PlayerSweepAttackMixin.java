package dasouza.telum.mixin;

import dasouza.telum.component.AssembledToolData;
import dasouza.telum.item.AssembledToolItem;
import dasouza.telum.tool.ToolType;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Player.class)
public abstract class PlayerSweepAttackMixin {

    @Redirect(
        method = "isSweepAttack",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/tags/TagKey;)Z")
    )
    private boolean telum$allowModularSwordSweep(ItemStack stack, TagKey<Item> tag) {
        if (stack.getItem() instanceof AssembledToolItem) {
            AssembledToolData data = AssembledToolItem.getToolData(stack);
            if (data != null) {
                return data.toolType() == ToolType.SWORD;
            }
            return false;
        }
        return stack.is(tag);
    }
}
