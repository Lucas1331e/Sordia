package dasouza.telum.mixin;

import dasouza.telum.component.AssembledToolData;
import dasouza.telum.item.AssembledToolItem;
import dasouza.telum.tool.PartMaterial;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityFireDamageMixin {

    @ModifyVariable(method = "hurt", at = @At("HEAD"), ordinal = 0, argsOnly = true, require = 0)
    private float telum$reduceBlazeFireDamage(float amount, DamageSource source) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (amount > 0.0f && source != null && source.is(DamageTypeTags.IS_FIRE)) {
            ItemStack main = entity.getItemBySlot(EquipmentSlot.MAINHAND);
            ItemStack off = entity.getItemBySlot(EquipmentSlot.OFFHAND);

            AssembledToolData mainData = AssembledToolItem.getToolData(main);
            AssembledToolData offData = AssembledToolItem.getToolData(off);

            int blazeLvl = 0;
            if (mainData != null) blazeLvl = Math.max(blazeLvl, mainData.getMaterialLevel(PartMaterial.BLAZE));
            if (offData != null) blazeLvl = Math.max(blazeLvl, offData.getMaterialLevel(PartMaterial.BLAZE));

            if (blazeLvl >= 3) {
                // Blaze Level 3: Reduce fire damage by 50%
                return amount * 0.5f;
            }
        }
        return amount;
    }
}
