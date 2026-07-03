package com.binaris.wizardry.core.networking.s2c;

import com.binaris.wizardry.WizardryMainMod;
import com.binaris.wizardry.client.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public record SpellGlyphPacketS2C(HashMap<ResourceLocation, String> names,
                                   HashMap<ResourceLocation, String> descriptions) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SpellGlyphPacketS2C> TYPE =
            new CustomPacketPayload.Type<>(WizardryMainMod.location("spell_glyph_packet"));

    public static final StreamCodec<FriendlyByteBuf, SpellGlyphPacketS2C> STREAM_CODEC =
            StreamCodec.of(SpellGlyphPacketS2C::write, SpellGlyphPacketS2C::read);

    public SpellGlyphPacketS2C {
        names = (names != null) ? names : new HashMap<>();
        descriptions = (descriptions != null) ? descriptions : new HashMap<>();
    }

    private static void write(FriendlyByteBuf pBuf, SpellGlyphPacketS2C packet) {
        // use the union of keys to avoid missing entries if one map has extra keys
        Set<ResourceLocation> keys = new HashSet<>(packet.names.keySet());
        keys.addAll(packet.descriptions.keySet());

        pBuf.writeVarInt(keys.size());

        for (ResourceLocation key : keys) {
            pBuf.writeResourceLocation(key);
            String name = packet.names.get(key);
            if (name == null) name = "";
            String desc = packet.descriptions.get(key);
            if (desc == null) desc = "";
            pBuf.writeUtf(name);
            pBuf.writeUtf(desc);
        }
    }

    private static SpellGlyphPacketS2C read(FriendlyByteBuf pBuf) {
        HashMap<ResourceLocation, String> names = new HashMap<>();
        HashMap<ResourceLocation, String> descriptions = new HashMap<>();

        int size = pBuf.readVarInt();

        for (int i = 0; i < size; i++) {
            ResourceLocation key = pBuf.readResourceLocation();
            String name = pBuf.readUtf();
            String description = pBuf.readUtf();
            names.put(key, name);
            descriptions.put(key, description);
        }

        return new SpellGlyphPacketS2C(names, descriptions);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final SpellGlyphPacketS2C packet, final IPayloadContext context) {
        context.enqueueWork(() -> ClientPacketHandler.handleSpellGlyph(packet));
    }

    public HashMap<ResourceLocation, String> getDescriptions() {
        return descriptions;
    }

    public HashMap<ResourceLocation, String> getNames() {
        return names;
    }
}
