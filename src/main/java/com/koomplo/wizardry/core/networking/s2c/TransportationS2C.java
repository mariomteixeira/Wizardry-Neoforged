package com.koomplo.wizardry.core.networking.s2c;

import com.koomplo.wizardry.WizardryMainMod;
import com.koomplo.wizardry.client.ClientPacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Sent to the dimension when a player is teleported by the transportation spell, for the arrival sound and
 * particles (1.12.2 PacketTransportation). {@code dismountEntityId} is -1 when nothing needs dismounting client-side.
 */
public record TransportationS2C(BlockPos destination, int dismountEntityId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<TransportationS2C> TYPE =
            new CustomPacketPayload.Type<>(WizardryMainMod.location("transportation"));

    public static final StreamCodec<FriendlyByteBuf, TransportationS2C> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, TransportationS2C::destination,
                    ByteBufCodecs.INT, TransportationS2C::dismountEntityId,
                    TransportationS2C::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final TransportationS2C packet, final IPayloadContext context) {
        context.enqueueWork(() -> ClientPacketHandler.handleTransportation(packet));
    }
}
