package dasouza.telum.mixin;

import dasouza.telum.component.AssembledToolData;
import dasouza.telum.item.AssembledToolItem;
import dasouza.telum.tool.PartMaterial;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityGreedDamageMixin {

    @ModifyVariable(method = "hurt", at = @At("HEAD"), ordinal = 0, argsOnly = true, require = 0)
    private float telum$modifyGreedDamage(float amount, DamageSource source) {
        if (amount <= 0.0f) return amount;

        LivingEntity target = (LivingEntity) (Object) this;
        float multiplier = 1.0f;

        // Amplifies damage taken by holder of Greed tool (+20% per Greed level)
        ItemStack targetMain = target.getItemBySlot(EquipmentSlot.MAINHAND);
        ItemStack targetOff = target.getItemBySlot(EquipmentSlot.OFFHAND);

        AssembledToolData targetMainData = AssembledToolItem.getToolData(targetMain);
        AssembledToolData targetOffData = AssembledToolItem.getToolData(targetOff);

        int targetGreed = 0;
        if (targetMainData != null) targetGreed = Math.max(targetGreed, targetMainData.getMaterialLevel(PartMaterial.GREED));
        if (targetOffData != null) targetGreed = Math.max(targetGreed, targetOffData.getMaterialLevel(PartMaterial.GREED));

        if (targetGreed >= 1) {
            multiplier += 0.20f * targetGreed;
        }

        // Amplifies damage dealt by attacker holding Greed tool (+20% per Greed level)
        if (source != null && source.getEntity() instanceof LivingEntity attacker) {
            ItemStack attackerMain = attacker.getItemBySlot(EquipmentSlot.MAINHAND);
            AssembledToolData attackerData = AssembledToolItem.getToolData(attackerMain);

            if (attackerData != null) {
                int attackerGreed = attackerData.getMaterialLevel(PartMaterial.GREED);
                if (attackerGreed >= 1) {
                    multiplier += 0.20f * attackerGreed;
                }
            }
        }

        return amount * multiplier;
    }
}
