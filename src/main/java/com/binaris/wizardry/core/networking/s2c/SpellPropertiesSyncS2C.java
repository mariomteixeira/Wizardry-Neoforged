package com.binaris.wizardry.core.networking.s2c;

import com.binaris.wizardry.WizardryMainMod;
import com.binaris.wizardry.api.content.spell.properties.SpellProperties;
import com.binaris.wizardry.client.ClientPacketHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

public record SpellPropertiesSyncS2C(
        Map<ResourceLocation, SpellProperties> propertiesMap) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SpellPropertiesSyncS2C> TYPE =
            new CustomPacketPayload.Type<>(WizardryMainMod.location("spell_properties_sync"));

    public static final StreamCodec<FriendlyByteBuf, SpellPropertiesSyncS2C> STREAM_CODEC =
            StreamCodec.of(SpellPropertiesSyncS2C::write, SpellPropertiesSyncS2C::read);

    private static void write(FriendlyByteBuf buf, SpellPropertiesSyncS2C packet) {
        buf.writeInt(packet.propertiesMap.size());
        for (Map.Entry<ResourceLocation, SpellProperties> entry : packet.propertiesMap.entrySet()) {
            buf.writeResourceLocation(entry.getKey());
            CompoundTag nbt = entry.getValue().toNbt();
            buf.writeNbt(nbt);
        }
    }

    private static SpellPropertiesSyncS2C read(FriendlyByteBuf buf) {
        int size = buf.readInt();
        Map<ResourceLocation, SpellProperties> propertiesMap = new HashMap<>();
        for (int i = 0; i < size; i++) {
            ResourceLocation spellId = buf.readResourceLocation();
            SpellProperties props = SpellProperties.fromNbt(buf.readNbt());
            propertiesMap.put(spellId, props);
        }
        return new SpellPropertiesSyncS2C(propertiesMap);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final SpellPropertiesSyncS2C packet, final IPayloadContext context) {
        context.enqueueWork(() -> ClientPacketHandler.handleSpellPropertiesSync(packet));
    }

    public Map<ResourceLocation, SpellProperties> getPropertiesMap() {
        return propertiesMap;
    }
}
