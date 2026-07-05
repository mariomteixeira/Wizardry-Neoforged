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
 * Used just for testing the network api
 */
public record TestParticlePacketS2C(BlockPos pos, int color) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<TestParticlePacketS2C> TYPE =
            new CustomPacketPayload.Type<>(WizardryMainMod.location("test_particle_packet"));

    public static final StreamCodec<FriendlyByteBuf, TestParticlePacketS2C> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, TestParticlePacketS2C::pos,
                    ByteBufCodecs.INT, TestParticlePacketS2C::color,
                    TestParticlePacketS2C::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final TestParticlePacketS2C packet, final IPayloadContext context) {
        context.enqueueWork(() -> ClientPacketHandler.handleTestParticle(packet));
    }

    public BlockPos getPos() {
        return pos;
    }

    public int getColor() {
        return color;
    }
}
