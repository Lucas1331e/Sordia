package dasouza.telum.network;

import dasouza.telum.Telum;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * S2C payload: Server tells client to open the lyre minigame screen.
 */
public record OpenLyreScreenPayload(
        int targetX, int targetY, int targetZ,
        int sourceBlockId, int resultBlockId
) implements CustomPacketPayload {

    public static final Type<OpenLyreScreenPayload> TYPE =
            new Type<>(Telum.id("open_lyre_screen"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenLyreScreenPayload> CODEC =
            CustomPacketPayload.codec(
                    (payload, buf) -> {
                        buf.writeInt(payload.targetX);
                        buf.writeInt(payload.targetY);
                        buf.writeInt(payload.targetZ);
                        buf.writeInt(payload.sourceBlockId);
                        buf.writeInt(payload.resultBlockId);
                    },
                    buf -> new OpenLyreScreenPayload(
                            buf.readInt(), buf.readInt(), buf.readInt(),
                            buf.readInt(), buf.readInt()
                    )
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
