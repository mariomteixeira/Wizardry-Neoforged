package com.binaris.wizardry.network;

import com.binaris.wizardry.WizardryMainMod;
import com.binaris.wizardry.client.ClientPacketHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PlayerCapabilitySyncPacketS2C(CapabilityType capabilityType,
                                             CompoundTag data) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PlayerCapabilitySyncPacketS2C> TYPE =
            new CustomPacketPayload.Type<>(WizardryMainMod.location("player_capability_sync"));

    // CapabilityType is written/read via its ordinal as a VarInt, matching FriendlyByteBuf#writeEnum/#readEnum.
    public static final StreamCodec<FriendlyByteBuf, PlayerCapabilitySyncPacketS2C> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, packet -> packet.capabilityType().ordinal(),
                    ByteBufCodecs.COMPOUND_TAG, PlayerCapabilitySyncPacketS2C::data,
                    (ordinal, data) -> new PlayerCapabilitySyncPacketS2C(CapabilityType.values()[ordinal], data));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final PlayerCapabilitySyncPacketS2C packet, final IPayloadContext context) {
        context.enqueueWork(() -> ClientPacketHandler.handlePlayerCapabilitySync(packet));
    }

    public CompoundTag getData() {
        return data;
    }

    public CapabilityType getType() {
        return capabilityType;
    }

    public enum CapabilityType {
        CAST_COMMAND,
        SPELL_MANAGER,
        WIZARD_DATA
    }
}
