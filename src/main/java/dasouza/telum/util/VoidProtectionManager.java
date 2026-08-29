package dasouza.telum.util;

import dasouza.telum.Telum;
import dasouza.telum.particle.TelumParticles;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import java.util.*;

/**
 * Server-side manager for Void Protection.
 * Tracks safe ground position for protected players and teleports them back
 * if they fall into the void within 3 minutes of playing the Void Protection Ballad.
 */
public final class VoidProtectionManager {

    public record ProtectedPlayerState(int remainingTicks, Vec3 lastSafePos, ServerLevel lastSafeLevel) {}

    private static final Map<UUID, ProtectedPlayerState> PROTECTED_PLAYERS = new HashMap<>();

    private VoidProtectionManager() {}

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            Iterator<Map.Entry<UUID, ProtectedPlayerState>> iter = PROTECTED_PLAYERS.entrySet().iterator();

            while (iter.hasNext()) {
                Map.Entry<UUID, ProtectedPlayerState> entry = iter.next();
                UUID uuid = entry.getKey();
                ProtectedPlayerState state = entry.getValue();

                ServerPlayer player = server.getPlayerList().getPlayer(uuid);
                if (player == null || !player.isAlive()) {
                    continue;
                }

                int newTicks = state.remainingTicks - 1;
                if (newTicks <= 0) {
                    iter.remove();
                    continue;
                }

                ServerLevel level = (ServerLevel) player.level();

                // Update last safe position if player is standing on solid ground above minY
                Vec3 currentPos = player.position();
                BlockPos belowPos = player.blockPosition().below();
                if (player.onGround() && player.getY() > level.getMinY() && level.getBlockState(belowPos).isSolid()) {
                    state = new ProtectedPlayerState(newTicks, currentPos, level);
                    entry.setValue(state);
                } else {
                    entry.setValue(new ProtectedPlayerState(newTicks, state.lastSafePos, state.lastSafeLevel));
                }

                // Check if player has fallen into the void
                if (player.getY() < level.getMinY() - 10) {
                    Vec3 safePos = state.lastSafePos != null ? state.lastSafePos : new Vec3(player.getX(), level.getMinY() + 10, player.getZ());
                    ServerLevel safeLevel = state.lastSafeLevel != null ? state.lastSafeLevel : level;

                    player.teleportTo(safeLevel, safePos.x, safePos.y, safePos.z, Set.of(), player.getYRot(), player.getXRot(), true);
                    player.fallDistance = 0.0f;
                    player.setDeltaMovement(Vec3.ZERO);

                    safeLevel.playSound(null, safePos.x, safePos.y, safePos.z,
                            SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.2f, 1.0f);
                    safeLevel.playSound(null, safePos.x, safePos.y, safePos.z,
                            SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.5f, 1.2f);

                    safeLevel.sendParticles(TelumParticles.CLOCK_PARTICLE,
                            safePos.x, safePos.y + 1.0, safePos.z,
                            35, 0.4, 0.6, 0.4, 0.05);
                }
            }
        });

        Telum.LOGGER.info("Initialized VoidProtectionManager");
    }

    public static void grantProtection(ServerPlayer player, int durationTicks) {
        Vec3 pos = player.position();
        ServerLevel level = (ServerLevel) player.level();
        PROTECTED_PLAYERS.put(player.getUUID(), new ProtectedPlayerState(durationTicks, pos, level));

        level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0f, 1.3f);
        level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.5f, 1.2f);

        level.sendParticles(TelumParticles.CLOCK_PARTICLE,
                pos.x, pos.y + 1.0, pos.z,
                25, 0.3, 0.5, 0.3, 0.05);
    }

    public static boolean isProtected(UUID playerUuid) {
        return PROTECTED_PLAYERS.containsKey(playerUuid);
    }
}
