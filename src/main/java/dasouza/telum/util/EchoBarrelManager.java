package dasouza.telum.util;

import net.minecraft.core.BlockPos;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages player-bound Echo Barrels.
 * Maps a player's UUID to their tuned master Echo Barrel location and unique 6-note song.
 */
public final class EchoBarrelManager {

    public record BoundBarrel(BlockPos masterPos, int[] songCode, int encodedSongInt) {}

    private static final Map<UUID, BoundBarrel> BOUND_BARRELS = new ConcurrentHashMap<>();

    private EchoBarrelManager() {}

    public static void bindPlayerBarrel(UUID playerUuid, BlockPos masterPos, int[] songCode, int encodedSongInt) {
        BOUND_BARRELS.put(playerUuid, new BoundBarrel(masterPos.immutable(), songCode, encodedSongInt));
    }

    public static BoundBarrel getBoundBarrel(UUID playerUuid) {
        return BOUND_BARRELS.get(playerUuid);
    }

    public static boolean hasBoundBarrel(UUID playerUuid) {
        return BOUND_BARRELS.containsKey(playerUuid);
    }

    public static void unbindPlayerBarrel(UUID playerUuid) {
        BOUND_BARRELS.remove(playerUuid);
    }
}
