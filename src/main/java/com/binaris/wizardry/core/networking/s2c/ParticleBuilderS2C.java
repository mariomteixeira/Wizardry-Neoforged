package com.binaris.wizardry.core.networking.s2c;

import com.binaris.wizardry.WizardryMainMod;
import com.binaris.wizardry.api.client.ParticleBuilder;
import com.binaris.wizardry.client.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ParticleBuilderS2C(ParticleBuilder.ParticleData data) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ParticleBuilderS2C> TYPE =
            new CustomPacketPayload.Type<>(WizardryMainMod.location("particle_builder"));

    public static final StreamCodec<FriendlyByteBuf, ParticleBuilderS2C> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> packet.data.write(buf),
            buf -> new ParticleBuilderS2C(ParticleBuilder.ParticleData.read(buf)));

    public ParticleBuilder.ParticleData getData() {
        return data;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final ParticleBuilderS2C packet, final IPayloadContext context) {
        context.enqueueWork(() -> ClientPacketHandler.handleParticleBuilder(packet));
    }
}
