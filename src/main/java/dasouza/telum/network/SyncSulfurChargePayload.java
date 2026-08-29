package dasouza.telum.network;

import dasouza.telum.Telum;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SyncSulfurChargePayload(int charge) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncSulfurChargePayload> TYPE =
            new CustomPacketPayload.Type<>(Telum.id("sync_sulfur_charge"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncSulfurChargePayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, SyncSulfurChargePayload::charge,
                    SyncSulfurChargePayload::new
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
