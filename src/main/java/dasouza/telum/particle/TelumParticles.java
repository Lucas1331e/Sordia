package dasouza.telum.particle;

import dasouza.telum.Telum;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.SimpleParticleType;

public class TelumParticles {

    public static final SimpleParticleType CLOCK_PARTICLE = FabricParticleTypes.simple();
    public static final SimpleParticleType SHRIEK_TEMPORAL_PARTICLE = FabricParticleTypes.simple();

    public static void initialize() {
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, Telum.id("clock_particle"), CLOCK_PARTICLE);
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, Telum.id("shriek_temporal"), SHRIEK_TEMPORAL_PARTICLE);
    }
}
