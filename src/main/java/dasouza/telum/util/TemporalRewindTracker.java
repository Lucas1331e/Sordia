package dasouza.telum.util;

import dasouza.telum.effect.TelumEffects;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class TemporalRewindTracker {

    private static final int MAX_SNAPSHOTS = 60; // 60 ticks = 3 seconds

    public record PlayerSnapshot(
            Vec3 pos,
            float yRot,
            float xRot,
            float health,
            List<MobEffectInstance> activeEffects
    ) {}

    private static final Map<UUID, Deque<PlayerSnapshot>> PLAYER_HISTORY = new HashMap<>();

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (player.isAlive()) {
                    recordSnapshot(player);
                }
            }
        });
    }

    private static void recordSnapshot(ServerPlayer player) {
        Deque<PlayerSnapshot> history = PLAYER_HISTORY.computeIfAbsent(player.getUUID(), k -> new ArrayDeque<>());

        List<MobEffectInstance> copyEffects = new ArrayList<>();
        for (MobEffectInstance effect : player.getActiveEffects()) {
            copyEffects.add(new MobEffectInstance(effect));
        }

        PlayerSnapshot snapshot = new PlayerSnapshot(
                player.position(),
                player.getYRot(),
                player.getXRot(),
                player.getHealth(),
                copyEffects
        );

        history.addLast(snapshot);

        while (history.size() > MAX_SNAPSHOTS) {
            history.removeFirst();
        }
    }

    public static void rewind(ServerPlayer player) {
        Deque<PlayerSnapshot> history = PLAYER_HISTORY.get(player.getUUID());
        if (history == null || history.isEmpty()) {
            // Cures Shatter if no history recorded yet
            if (TelumEffects.SHATTER != null) {
                player.removeEffect(TelumEffects.SHATTER);
            }
            return;
        }

        // Get snapshot from 3 seconds ago (first in queue)
        PlayerSnapshot target = history.peekFirst();
        if (target == null) return;

        ServerLevel level = (ServerLevel) player.level();

        // 1. Teleport player back to 3-second location
        player.teleportTo(level, target.pos().x, target.pos().y, target.pos().z, Set.of(), target.yRot(), target.xRot(), true);

        // 2. Remove all active effects (clearing Shatter!)
        player.removeAllEffects();

        // 3. Re-apply status effects from 3 seconds ago (excluding Shatter)
        for (MobEffectInstance effect : target.activeEffects()) {
            if (TelumEffects.SHATTER == null || !effect.getEffect().equals(TelumEffects.SHATTER)) {
                player.addEffect(new MobEffectInstance(effect));
            }
        }

        // 4. Restore health to 3-second state
        float targetHealth = Math.min(target.health(), player.getMaxHealth());
        player.setHealth(targetHealth);

        // 5. Spawn clock particles
        level.sendParticles(dasouza.telum.particle.TelumParticles.CLOCK_PARTICLE, target.pos().x, target.pos().y + 1.0, target.pos().z, 45, 0.5, 0.6, 0.5, 0.05);

        // 6. Play glass breaking sound effect
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.2f, 1.0f);
    }
}
