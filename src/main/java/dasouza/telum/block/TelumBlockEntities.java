package dasouza.telum.block;

import dasouza.telum.Telum;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Set;

/**
 * Central registry for all Telum block entity types.
 */
public final class TelumBlockEntities {

    public static BlockEntityType<ArcheologyTableBlockEntity> ARCHEOLOGY_TABLE_ENTITY;
    public static BlockEntityType<EchoBarrelBlockEntity> ECHO_BARREL_ENTITY;
    public static BlockEntityType<EchoProjectionBlockEntity> ECHO_PROJECTION_ENTITY;

    public static void initialize() {
        Telum.LOGGER.info("Registering Telum block entities");

        ARCHEOLOGY_TABLE_ENTITY = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                Telum.id("archeology_table"),
                new BlockEntityType<>(ArcheologyTableBlockEntity::new, Set.of(TelumBlocks.ARCHEOLOGY_TABLE))
        );

        ECHO_BARREL_ENTITY = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                Telum.id("echo_barrel"),
                new BlockEntityType<>(EchoBarrelBlockEntity::new, Set.of(TelumBlocks.ECHO_BARREL))
        );

        ECHO_PROJECTION_ENTITY = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                Telum.id("echo_barrel_projection"),
                new BlockEntityType<>(EchoProjectionBlockEntity::new, Set.of(TelumBlocks.ECHO_PROJECTION_BARREL))
        );
    }
}
