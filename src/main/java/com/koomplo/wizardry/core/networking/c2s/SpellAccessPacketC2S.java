package com.koomplo.wizardry.core.networking.c2s;

import com.koomplo.wizardry.WizardryMainMod;
import com.koomplo.wizardry.api.content.item.ICastItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SpellAccessPacketC2S(int index) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SpellAccessPacketC2S> TYPE =
            new CustomPacketPayload.Type<>(WizardryMainMod.location("spell_quick_access"));

    public static final StreamCodec<FriendlyByteBuf, SpellAccessPacketC2S> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, SpellAccessPacketC2S::index,
                    SpellAccessPacketC2S::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final SpellAccessPacketC2S packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();

            ItemStack wand = player.getMainHandItem();

            if (!(wand.getItem() instanceof ICastItem)) {
                wand = player.getOffhandItem();
            }

            if (wand.getItem() instanceof ICastItem castItem) {
                castItem.selectSpell(wand, packet.index());
                player.stopUsingItem();
            }
        });
    }
}
