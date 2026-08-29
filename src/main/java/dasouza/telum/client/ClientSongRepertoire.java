package dasouza.telum.client;

import dasouza.telum.network.SyncPlayerSongsPayload;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Client-side song repertoire cache for the local player.
 */
public final class ClientSongRepertoire {

    private static final List<SyncPlayerSongsPayload.SongData> KNOWN_SONGS = new ArrayList<>();
    private static final Set<String> LEARNED_SONG_IDS = new HashSet<>();

    private ClientSongRepertoire() {}

    public static void setPayload(SyncPlayerSongsPayload payload) {
        KNOWN_SONGS.clear();
        KNOWN_SONGS.addAll(payload.songs());

        LEARNED_SONG_IDS.clear();
        if (payload.learnedSongIds() != null) {
            LEARNED_SONG_IDS.addAll(payload.learnedSongIds());
        }
    }

    public static void setSongs(List<SyncPlayerSongsPayload.SongData> songs) {
        KNOWN_SONGS.clear();
        KNOWN_SONGS.addAll(songs);
    }

    public static List<SyncPlayerSongsPayload.SongData> getSongs() {
        return KNOWN_SONGS;
    }

    public static boolean hasLearnedSongId(String songId) {
        return LEARNED_SONG_IDS.contains(songId);
    }

    public static boolean containsSongCode(int code) {
        for (SyncPlayerSongsPayload.SongData song : KNOWN_SONGS) {
            if (song.encodedSongInt() == code) {
                return true;
            }
        }
        return false;
    }
}
