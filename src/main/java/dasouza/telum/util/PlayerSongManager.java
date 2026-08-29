package dasouza.telum.util;

import net.minecraft.core.BlockPos;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages custom Echo Barrel songs bound to individual players.
 * Songs belong to the player's memory, not the item!
 */
public final class PlayerSongManager {

    public record BarrelSongEntry(BlockPos masterPos, String title, int[] notes, int encodedSongInt) {}

    private static final Map<UUID, List<BarrelSongEntry>> PLAYER_SONGS = new ConcurrentHashMap<>();
    private static final Map<UUID, Set<String>> LEARNED_SONG_IDS = new ConcurrentHashMap<>();

    public static boolean hasLearnedSong(UUID playerUuid, String songId) {
        Set<String> learned = LEARNED_SONG_IDS.get(playerUuid);
        return learned != null && learned.contains(songId);
    }

    public static void learnSong(UUID playerUuid, String songId) {
        Set<String> learned = LEARNED_SONG_IDS.computeIfAbsent(playerUuid, k -> ConcurrentHashMap.newKeySet());
        learned.add(songId);
    }

    public static Set<String> getLearnedSongIds(UUID playerUuid) {
        return LEARNED_SONG_IDS.getOrDefault(playerUuid, Collections.emptySet());
    }

    private static final String[] RHYMING_WORDS = {
            "Místico", "Susurrante", "Celestial", "Etéreo", "Luminoso",
            "Radiante", "Armónico", "Infinito", "Arcano", "Sombrío",
            "Estelar", "Resonante", "Vértice", "Velo", "Astral", "Enigma"
    };

    private static final Random RANDOM = new Random();

    private PlayerSongManager() {}

    public static String generateRandomBarrelTitle() {
        String word = RHYMING_WORDS[RANDOM.nextInt(RHYMING_WORDS.length)];
        return "Canción del Barril " + word;
    }

    public static List<BarrelSongEntry> getPlayerSongs(UUID playerUuid) {
        return PLAYER_SONGS.getOrDefault(playerUuid, Collections.emptyList());
    }

    public static boolean isSongAlreadyBound(UUID playerUuid, int encodedSongInt) {
        List<BarrelSongEntry> songs = PLAYER_SONGS.get(playerUuid);
        if (songs != null) {
            for (BarrelSongEntry entry : songs) {
                if (entry.encodedSongInt() == encodedSongInt) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void addOrUpdatePlayerSong(UUID playerUuid, BlockPos masterPos, String title, int[] notes, int encodedSongInt) {
        List<BarrelSongEntry> songs = PLAYER_SONGS.computeIfAbsent(playerUuid, k -> new ArrayList<>());

        // Remove existing song for this position if updating
        songs.removeIf(entry -> entry.masterPos().equals(masterPos));

        songs.add(new BarrelSongEntry(masterPos.immutable(), title, notes, encodedSongInt));
    }

    public static void removeSongByPos(BlockPos pos) {
        BlockPos immutablePos = pos.immutable();
        for (Map.Entry<UUID, List<BarrelSongEntry>> entry : PLAYER_SONGS.entrySet()) {
            entry.getValue().removeIf(song -> song.masterPos().equals(immutablePos));
        }
    }

    public static BarrelSongEntry getSongForPos(UUID playerUuid, BlockPos pos) {
        List<BarrelSongEntry> songs = PLAYER_SONGS.get(playerUuid);
        if (songs != null) {
            for (BarrelSongEntry song : songs) {
                if (song.masterPos().equals(pos)) {
                    return song;
                }
            }
        }
        return null;
    }

    public static BarrelSongEntry getSongByCode(UUID playerUuid, int encodedSongInt) {
        List<BarrelSongEntry> songs = PLAYER_SONGS.get(playerUuid);
        if (songs != null) {
            for (BarrelSongEntry song : songs) {
                if (song.encodedSongInt() == encodedSongInt) {
                    return song;
                }
            }
        }
        return null;
    }
}
