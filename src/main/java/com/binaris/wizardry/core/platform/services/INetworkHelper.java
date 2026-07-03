package com.binaris.wizardry.core.platform.services;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public interface INetworkHelper {
    <T extends CustomPacketPayload> void sendTo(ServerPlayer pPlayer, T pMessage);

    <T extends CustomPacketPayload> void sendToServer(T pMessage);

    <T extends CustomPacketPayload> void sendToTracking(ServerLevel pLevel, BlockPos pPos, T pMessage);

    <T extends CustomPacketPayload> void sendToTracking(Entity pEntity, T pMessage);

    <T extends CustomPacketPayload> void sendToDimension(MinecraftServer server, T packet, ResourceKey<Level> dimension);
}
