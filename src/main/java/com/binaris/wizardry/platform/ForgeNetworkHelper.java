package com.binaris.wizardry.platform;

import com.binaris.wizardry.core.platform.services.INetworkHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

public class ForgeNetworkHelper implements INetworkHelper {
    @Override
    public <T extends CustomPacketPayload> void sendTo(ServerPlayer pPlayer, T pMessage) {
        PacketDistributor.sendToPlayer(pPlayer, pMessage);
    }

    @Override
    public <T extends CustomPacketPayload> void sendToServer(T pMessage) {
        PacketDistributor.sendToServer(pMessage);
    }

    @Override
    public <T extends CustomPacketPayload> void sendToTracking(ServerLevel pLevel, BlockPos pPos, T pMessage) {
        PacketDistributor.sendToPlayersTrackingChunk(pLevel, new ChunkPos(pPos), pMessage);
    }

    @Override
    public <T extends CustomPacketPayload> void sendToTracking(Entity pEntity, T pMessage) {
        PacketDistributor.sendToPlayersTrackingEntity(pEntity, pMessage);
    }

    @Override
    public <T extends CustomPacketPayload> void sendToDimension(MinecraftServer server, T packet, ResourceKey<Level> dimension) {
        if (server == null) return;
        ServerLevel level = server.getLevel(dimension);
        if (level == null) return;
        PacketDistributor.sendToPlayersInDimension(level, packet);
    }
}
