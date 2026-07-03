package com.binaris.wizardry.network;

import com.binaris.wizardry.WizardryMainMod;
import com.binaris.wizardry.client.ClientPacketHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ContainmentSyncPacketS2C(int entityId, CompoundTag data) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ContainmentSyncPacketS2C> TYPE =
            new CustomPacketPayload.Type<>(WizardryMainMod.location("containment_sync"));

    public static final StreamCodec<FriendlyByteBuf, ContainmentSyncPacketS2C> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, ContainmentSyncPacketS2C::entityId,
                    ByteBufCodecs.COMPOUND_TAG, ContainmentSyncPacketS2C::data,
                    ContainmentSyncPacketS2C::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final ContainmentSyncPacketS2C packet, final IPayloadContext context) {
        context.enqueueWork(() -> ClientPacketHandler.handleContainmentSync(packet));
    }

    public CompoundTag getData() {
        return data;
    }

    public int getEntityId() {
        return entityId;
    }
}
