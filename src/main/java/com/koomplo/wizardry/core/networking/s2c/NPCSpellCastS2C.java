package com.koomplo.wizardry.core.networking.s2c;

import com.koomplo.wizardry.WizardryMainMod;
import com.koomplo.wizardry.api.content.spell.Spell;
import com.koomplo.wizardry.api.content.spell.internal.SpellModifiers;
import com.koomplo.wizardry.client.ClientPacketHandler;
import com.koomplo.wizardry.core.platform.Services;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record NPCSpellCastS2C(int casterID, int targetID, InteractionHand hand, Spell spell,
                               SpellModifiers modifiers) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<NPCSpellCastS2C> TYPE =
            new CustomPacketPayload.Type<>(WizardryMainMod.location("npc_spell_cast"));

    public static final StreamCodec<FriendlyByteBuf, NPCSpellCastS2C> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, NPCSpellCastS2C::casterID,
                    ByteBufCodecs.INT, NPCSpellCastS2C::targetID,
                    ByteBufCodecs.BOOL, packet -> packet.hand() == InteractionHand.MAIN_HAND,
                    ResourceLocation.STREAM_CODEC, packet -> packet.spell().getLocation(),
                    ByteBufCodecs.COMPOUND_TAG, packet -> packet.modifiers().toTag(),
                    (casterID, targetID, mainHand, spellId, modifiersTag) -> new NPCSpellCastS2C(
                            casterID,
                            targetID,
                            mainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND,
                            Services.REGISTRY_UTIL.getSpell(spellId),
                            SpellModifiers.fromTag(modifiersTag)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final NPCSpellCastS2C packet, final IPayloadContext context) {
        context.enqueueWork(() -> ClientPacketHandler.handleNPCSpellCast(packet));
    }

    public InteractionHand getHand() {
        return hand;
    }

    public Spell getSpell() {
        return spell;
    }

    public SpellModifiers getModifiers() {
        return modifiers;
    }

    public int getCasterID() {
        return casterID;
    }

    public int getTargetID() {
        return targetID;
    }
}
