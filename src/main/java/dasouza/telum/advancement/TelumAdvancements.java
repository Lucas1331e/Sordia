package dasouza.telum.advancement;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public final class TelumAdvancements {

    private TelumAdvancements() {}

    public static void grantAdvancement(ServerPlayer player, Identifier advancementId) {
        if (player == null || player.level() == null || player.level().getServer() == null) return;
        AdvancementHolder holder = player.level().getServer().getAdvancements().get(advancementId);
        if (holder != null) {
            AdvancementProgress progress = player.getAdvancements().getOrStartProgress(holder);
            if (!progress.isDone()) {
                for (String criterion : progress.getRemainingCriteria()) {
                    player.getAdvancements().award(holder, criterion);
                }
            }
        }
    }
}
