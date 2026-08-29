package dasouza.telum.network;

import dasouza.telum.Telum;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * C2S payload: Client tells server the result of the lyre minigame.
 */
public record LyreGameResultPayload(
        boolean success,
        int score,
        int targetX, int targetY, int targetZ,
        int resultBlockId
) implements CustomPacketPayload {

    public static final Type<LyreGameResultPayload> TYPE =
            new Type<>(Telum.id("lyre_game_result"));

    public static final StreamCodec<RegistryFriendlyByteBuf, LyreGameResultPayload> CODEC =
            CustomPacketPayload.codec(
                    (payload, buf) -> {
                        buf.writeBoolean(payload.success);
                        buf.writeInt(payload.score);
                        buf.writeInt(payload.targetX);
                        buf.writeInt(payload.targetY);
                        buf.writeInt(payload.targetZ);
                        buf.writeInt(payload.resultBlockId);
                    },
                    buf -> new LyreGameResultPayload(
                            buf.readBoolean(), buf.readInt(),
                            buf.readInt(), buf.readInt(), buf.readInt(),
                            buf.readInt()
                    )
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
