package com.koomplo.wizardry.core.networking.s2c;

import com.koomplo.wizardry.WizardryMainMod;
import com.koomplo.wizardry.client.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ScreenShakeS2C(float intensity, int duration) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ScreenShakeS2C> TYPE =
            new CustomPacketPayload.Type<>(WizardryMainMod.location("screen_shake"));

    public static final StreamCodec<FriendlyByteBuf, ScreenShakeS2C> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.FLOAT, ScreenShakeS2C::intensity,
                    ByteBufCodecs.INT, ScreenShakeS2C::duration,
                    ScreenShakeS2C::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final ScreenShakeS2C packet, final IPayloadContext context) {
        context.enqueueWork(() -> ClientPacketHandler.handleScreenShake(packet));
    }
}
