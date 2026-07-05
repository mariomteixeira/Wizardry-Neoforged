package com.koomplo.wizardry.core.networking.s2c;

import com.koomplo.wizardry.WizardryMainMod;
import com.koomplo.wizardry.client.ClientPacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

/** Sends the found clairvoyance path to the caster's client for the guiding particle trail. */
public record ClairvoyanceS2C(List<BlockPos> points, float durationMultiplier) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ClairvoyanceS2C> TYPE =
            new CustomPacketPayload.Type<>(WizardryMainMod.location("clairvoyance"));

    public static final StreamCodec<FriendlyByteBuf, ClairvoyanceS2C> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.collection(java.util.ArrayList::new, BlockPos.STREAM_CODEC), ClairvoyanceS2C::points,
                    ByteBufCodecs.FLOAT, ClairvoyanceS2C::durationMultiplier,
                    ClairvoyanceS2C::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final ClairvoyanceS2C packet, final IPayloadContext context) {
        context.enqueueWork(() -> ClientPacketHandler.handleClairvoyance(packet));
    }
}
