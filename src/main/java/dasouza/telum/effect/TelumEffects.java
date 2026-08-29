package dasouza.telum.effect;

import dasouza.telum.Telum;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;

public class TelumEffects {

    public static Holder<MobEffect> SHATTER;

    public static void initialize() {
        SHATTER = Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Telum.id("shatter"), new ShatterMobEffect());
    }
}
