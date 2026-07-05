package com.binaris.wizardry.core.networking.s2c;

import com.binaris.wizardry.WizardryMainMod;
import com.binaris.wizardry.client.ClientPacketHandler;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Syncs possession start/end so clients can render the possessing player as the possessed creature
 * (1.12.2 PacketPossession). An empty entity type id means the possession ended.
 */
public record PossessionS2C(int playerId, String entityTypeId, CompoundTag entityNbt, int duration) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PossessionS2C> TYPE =
            new CustomPacketPayload.Type<>(WizardryMainMod.location("possession"));

    public static final StreamCodec<FriendlyByteBuf, PossessionS2C> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, PossessionS2C::playerId,
                    ByteBufCodecs.STRING_UTF8, PossessionS2C::entityTypeId,
                    ByteBufCodecs.COMPOUND_TAG, PossessionS2C::entityNbt,
                    ByteBufCodecs.INT, PossessionS2C::duration,
                    PossessionS2C::new);

    public static PossessionS2C start(Player possessor, Mob target, int duration) {
        CompoundTag tag = new CompoundTag();
        target.saveWithoutId(tag);
        return new PossessionS2C(possessor.getId(),
                BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()).toString(), tag, duration);
    }

    public static PossessionS2C end(Player possessor) {
        return new PossessionS2C(possessor.getId(), "", new CompoundTag(), 0);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final PossessionS2C packet, final IPayloadContext context) {
        context.enqueueWork(() -> ClientPacketHandler.handlePossession(packet));
    }
}
