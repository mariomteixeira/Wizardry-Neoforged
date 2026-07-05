package com.koomplo.wizardry.core.networking.c2s;

import com.koomplo.wizardry.WizardryMainMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record BlockUsePacketC2S(BlockPos pos) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<BlockUsePacketC2S> TYPE =
            new CustomPacketPayload.Type<>(WizardryMainMod.location("block_use_packet"));

    public static final StreamCodec<FriendlyByteBuf, BlockUsePacketC2S> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, BlockUsePacketC2S::pos,
                    BlockUsePacketC2S::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final BlockUsePacketC2S packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            MinecraftServer server = player.server;

            Pig pig = EntityType.PIG.create(server.getLevel(Level.OVERWORLD));
            if (pig != null) {
                pig.setPos(packet.pos().getX(), packet.pos().getY(), packet.pos().getZ());
                pig.setAge(0);
                server.getLevel(Level.OVERWORLD).addFreshEntity(pig);
            }
        });
    }
}
