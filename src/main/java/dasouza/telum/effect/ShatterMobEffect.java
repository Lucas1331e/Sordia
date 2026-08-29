package dasouza.telum.effect;

import dasouza.telum.Telum;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class ShatterMobEffect extends MobEffect {

    public ShatterMobEffect() {
        super(MobEffectCategory.HARMFUL, 0x4A0E17);
        this.addAttributeModifier(
                Attributes.MAX_HEALTH,
                Telum.id("shatter_max_health_reduction"),
                -2.0,
                AttributeModifier.Operation.ADD_VALUE
        );
    }
}
