package dasouza.telum.network;

import dasouza.telum.Telum;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.ArrayList;
import java.util.List;

public record SyncPlayerSongsPayload(List<SongData> songs, List<String> learnedSongIds) implements CustomPacketPayload {

    public record SongData(int x, int y, int z, String title, int encodedSongInt) {}

    public static final Type<SyncPlayerSongsPayload> TYPE =
            new Type<>(Telum.id("sync_player_songs"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncPlayerSongsPayload> CODEC =
            CustomPacketPayload.codec(
                    (payload, buf) -> {
                        buf.writeInt(payload.songs().size());
                        for (SongData song : payload.songs()) {
                            buf.writeInt(song.x());
                            buf.writeInt(song.y());
                            buf.writeInt(song.z());
                            buf.writeUtf(song.title());
                            buf.writeInt(song.encodedSongInt());
                        }
                        buf.writeInt(payload.learnedSongIds().size());
                        for (String id : payload.learnedSongIds()) {
                            buf.writeUtf(id);
                        }
                    },
                    buf -> {
                        int count = buf.readInt();
                        List<SongData> list = new ArrayList<>();
                        for (int i = 0; i < count; i++) {
                            int x = buf.readInt();
                            int y = buf.readInt();
                            int z = buf.readInt();
                            String title = buf.readUtf();
                            int code = buf.readInt();
                            list.add(new SongData(x, y, z, title, code));
                        }
                        int learnedCount = buf.readInt();
                        List<String> learnedList = new ArrayList<>();
                        for (int i = 0; i < learnedCount; i++) {
                            learnedList.add(buf.readUtf());
                        }
                        return new SyncPlayerSongsPayload(list, learnedList);
                    }
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
