package dasouza.telum.screen;

import dasouza.telum.Telum;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

/**
 * Registers all custom screen handler (menu) types for the Telum mod.
 */
public final class TelumScreenHandlers {

    public static MenuType<ForgeScreenHandler> FORGE_SCREEN_HANDLER;

    public static void initialize() {
        Telum.LOGGER.info("Registering Telum screen handlers");

        FORGE_SCREEN_HANDLER = Registry.register(
                BuiltInRegistries.MENU,
                Telum.id("telum_forge"),
                new MenuType<>(ForgeScreenHandler::new, FeatureFlags.DEFAULT_FLAGS)
        );
    }
}
