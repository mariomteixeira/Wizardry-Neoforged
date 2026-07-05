package com.binaris.wizardry.core.networking.s2c;

import com.binaris.wizardry.WizardryMainMod;
import com.binaris.wizardry.client.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Sent to the dimension when a player is resurrected, so clients revive their copy (1.12.2 PacketResurrection). */
public record ResurrectionS2C(int entityId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ResurrectionS2C> TYPE =
            new CustomPacketPayload.Type<>(WizardryMainMod.location("resurrection"));

    public static final StreamCodec<FriendlyByteBuf, ResurrectionS2C> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.INT, ResurrectionS2C::entityId, ResurrectionS2C::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final ResurrectionS2C packet, final IPayloadContext context) {
        context.enqueueWork(() -> ClientPacketHandler.handleResurrection(packet));
    }
}
