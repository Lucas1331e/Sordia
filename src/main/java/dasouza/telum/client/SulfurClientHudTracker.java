package dasouza.telum.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class SulfurClientHudTracker {

    private static int currentCharge = 0;
    private static int displayTicks = 0;

    private SulfurClientHudTracker() {}

    public static void onFullHit(int charge) {
        currentCharge = charge;
        displayTicks = 40; // 2.0 seconds (40 ticks)
    }

    public static void clientTick() {
        if (displayTicks > 0) {
            displayTicks--;
        }
    }

    public static int getCurrentCharge() {
        return currentCharge;
    }

    public static int getDisplayTicks() {
        return displayTicks;
    }

    public static void resetCharge() {
        currentCharge = 0;
        displayTicks = 0;
    }
}
