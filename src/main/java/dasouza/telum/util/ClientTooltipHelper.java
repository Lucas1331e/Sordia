package dasouza.telum.util;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/**
 * Server-safe helper for checking client UI state (Shift key) in item tooltips.
 */
public final class ClientTooltipHelper {

    private ClientTooltipHelper() {}

    public static boolean isShiftDown() {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            return isShiftDownClient();
        }
        return false;
    }

    private static boolean isShiftDownClient() {
        var window = Minecraft.getInstance().getWindow();
        if (window == null) return false;
        return InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SHIFT) ||
               InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
    }
}
