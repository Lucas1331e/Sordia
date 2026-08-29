package dasouza.telum.entity;

import dasouza.telum.Telum;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class TelumEntities {

    public static final ResourceKey<EntityType<?>> TEMPORAL_EVOKER_FANGS_KEY = ResourceKey.create(
            Registries.ENTITY_TYPE, Telum.id("temporal_evoker_fangs")
    );

    public static EntityType<TemporalEvokerFangs> TEMPORAL_EVOKER_FANGS;

    public static void initialize() {
        Telum.LOGGER.info("Registering Telum entities");

        TEMPORAL_EVOKER_FANGS = Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                TEMPORAL_EVOKER_FANGS_KEY,
                EntityType.Builder.<TemporalEvokerFangs>of(TemporalEvokerFangs::new, MobCategory.MISC)
                        .sized(0.5F, 0.8F)
                        .clientTrackingRange(6)
                        .updateInterval(2)
                        .build(TEMPORAL_EVOKER_FANGS_KEY)
        );
    }
}
