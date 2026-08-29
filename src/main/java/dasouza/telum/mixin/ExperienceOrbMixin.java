package dasouza.telum.mixin;

import dasouza.telum.item.AssembledToolItem;
import dasouza.telum.component.AssembledToolData;
import dasouza.telum.tool.PartMaterial;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ExperienceOrb.class)
public class ExperienceOrbMixin {

    @Inject(method = "repairPlayerItems", at = @At("HEAD"), cancellable = true)
    private void onRepairPlayerItems(ServerPlayer player, int amount, CallbackInfoReturnable<Integer> cir) {
        if (hasDamagedSkulkToolInHand(player)) {
            int remainingXp = repairSkulkToolsInHand(player, amount);
            cir.setReturnValue(remainingXp);
        }
    }

    private static boolean hasDamagedSkulkToolInHand(Player player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof AssembledToolItem && stack.isDamaged()) {
                AssembledToolData data = AssembledToolItem.getToolData(stack);
                if (data != null && data.getMaterialLevel(PartMaterial.SKULK) >= 1) {
                    return true;
                }
            }
        }
        return false;
    }

    private static int repairSkulkToolsInHand(Player player, int xpValue) {
        if (xpValue <= 0) return 0;

        // Check Mainhand first, then Offhand
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof AssembledToolItem && stack.isDamaged()) {
                AssembledToolData data = AssembledToolItem.getToolData(stack);
                if (data != null) {
                    int skulkLvl = data.getMaterialLevel(PartMaterial.SKULK);
                    if (skulkLvl >= 1) {
                        int durPerXp = 2 * skulkLvl;
                        int damage = stack.getDamageValue();
                        int xpNeeded = (int) Math.ceil((double) damage / durPerXp);
                        int xpToUse = Math.min(xpValue, xpNeeded);
                        int repaired = xpToUse * durPerXp;

                        stack.setDamageValue(Math.max(0, damage - repaired));
                        xpValue -= xpToUse;
                        if (xpValue <= 0) return 0;
                    }
                }
            }
        }

        return xpValue;
    }
}
