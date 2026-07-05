package com.koomplo.wizardry.core.networking.c2s;

import com.koomplo.wizardry.WizardryMainMod;
import com.koomplo.wizardry.api.content.item.ICastItem;
import com.koomplo.wizardry.content.menu.ArcaneWorkbenchMenu;
import com.koomplo.wizardry.core.EBLogger;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ControlInputPacketC2S(ControlType controlType) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ControlInputPacketC2S> TYPE =
            new CustomPacketPayload.Type<>(WizardryMainMod.location("control_input"));

    public static final StreamCodec<FriendlyByteBuf, ControlInputPacketC2S> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, packet -> packet.controlType().ordinal(),
                    ordinal -> new ControlInputPacketC2S(ControlType.values()[ordinal]));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final ControlInputPacketC2S packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();

            ItemStack wand = player.getMainHandItem();

            if (!(wand.getItem() instanceof ICastItem)) {
                wand = player.getOffhandItem();
            }

            switch (packet.controlType()) {
                case NEXT_SPELL_KEY:
                    if (wand.getItem() instanceof ICastItem castItem) {
                        castItem.selectNextSpell(wand);
                        player.stopUsingItem();
                    }
                    break;
                case PREVIOUS_SPELL_KEY:
                    if (wand.getItem() instanceof ICastItem castItem) {
                        castItem.selectPreviousSpell(wand);
                        player.stopUsingItem();
                    }
                    break;
                case APPLY_BUTTON:
                    if (!(player.containerMenu instanceof ArcaneWorkbenchMenu menu)) {
                        EBLogger.warn("Received a ControlInputPacketC2S, but the player that sent it was not currently using an arcane workbench. This should not happen!");
                    } else {
                        menu.onApplyButtonPressed(player);
                    }
                    break;
                case CLEAR_BUTTON:
                    if (!(player.containerMenu instanceof ArcaneWorkbenchMenu menu)) {
                        EBLogger.warn("Received a ControlInputPacketC2S, but the player that sent it was not currently using an arcane workbench. This should not happen!");
                    } else {
                        menu.onClearButtonPressed(player);
                    }
                    break;
                case POSSESSION_PROJECTILE:
                    com.koomplo.wizardry.content.spell.necromancy.Possession.shootProjectile(player);
            }
        });
    }

    public enum ControlType {
        APPLY_BUTTON, NEXT_SPELL_KEY,
        PREVIOUS_SPELL_KEY, RESURRECT_BUTTON,
        CLEAR_BUTTON, POSSESSION_PROJECTILE
    }
}
