package dasouza.telum.network;

import dasouza.telum.Telum;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.ArrayList;
import java.util.List;

public record SyncBookProgressPayload(List<String> craftedMaterials, List<String> craftedTools) implements CustomPacketPayload {

    public static final Type<SyncBookProgressPayload> TYPE =
            new Type<>(Telum.id("sync_book_progress"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncBookProgressPayload> CODEC =
            CustomPacketPayload.codec(
                    (payload, buf) -> {
                        buf.writeInt(payload.craftedMaterials().size());
                        for (String mat : payload.craftedMaterials()) {
                            buf.writeUtf(mat);
                        }
                        buf.writeInt(payload.craftedTools().size());
                        for (String tool : payload.craftedTools()) {
                            buf.writeUtf(tool);
                        }
                    },
                    buf -> {
                        int craftedCount = buf.readInt();
                        List<String> craftedList = new ArrayList<>();
                        for (int i = 0; i < craftedCount; i++) {
                            craftedList.add(buf.readUtf());
                        }
                        int toolsCount = buf.readInt();
                        List<String> toolsList = new ArrayList<>();
                        for (int i = 0; i < toolsCount; i++) {
                            toolsList.add(buf.readUtf());
                        }
                        return new SyncBookProgressPayload(craftedList, toolsList);
                    }
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
