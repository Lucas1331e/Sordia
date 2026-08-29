package dasouza.telum.component;

import dasouza.telum.Telum;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;

/**
 * Registers all custom DataComponentTypes for the Telum mod.
 */
public final class TelumComponents {

    /**
     * Component stored on individual tool parts.
     * Contains the part type and tier.
     */
    public static final DataComponentType<ToolPartData> TOOL_PART = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Telum.id("tool_part"),
            DataComponentType.<ToolPartData>builder()
                    .persistent(ToolPartData.CODEC)
                    .build()
    );

    /**
     * Component stored on assembled tools.
     * Contains all 3 parts, tool type, and computed stats.
     */
    public static final DataComponentType<AssembledToolData> ASSEMBLED_TOOL = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Telum.id("assembled_tool"),
            DataComponentType.<AssembledToolData>builder()
                    .persistent(AssembledToolData.CODEC)
                    .build()
    );

    /**
     * Component stored on assembled tools to track active Gilded Frenzy speed stacks.
     */
    public static final DataComponentType<GildedFrenzyData> GILDED_FRENZY = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Telum.id("gilded_frenzy"),
            DataComponentType.<GildedFrenzyData>builder()
                    .persistent(GildedFrenzyData.CODEC)
                    .build()
    );


    /**
     * Forces static initialization of this class, triggering registration.
     */
    public static void initialize() {
        Telum.LOGGER.info("Registering Telum data components");
    }
}
