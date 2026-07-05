package com.koomplo.wizardry.network;

import com.koomplo.wizardry.WizardryMainMod;
import com.koomplo.wizardry.client.ClientPacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ArcaneLockSyncPacketS2C(BlockPos pos, CompoundTag data) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ArcaneLockSyncPacketS2C> TYPE =
            new CustomPacketPayload.Type<>(WizardryMainMod.location("arcane_lock_sync"));

    public static final StreamCodec<FriendlyByteBuf, ArcaneLockSyncPacketS2C> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, ArcaneLockSyncPacketS2C::pos,
                    ByteBufCodecs.COMPOUND_TAG, ArcaneLockSyncPacketS2C::data,
                    ArcaneLockSyncPacketS2C::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final ArcaneLockSyncPacketS2C packet, final IPayloadContext context) {
        context.enqueueWork(() -> ClientPacketHandler.handleArcaneLock(packet));
    }

    public BlockPos getPos() {
        return pos;
    }

    public CompoundTag getData() {
        return data;
    }
}
