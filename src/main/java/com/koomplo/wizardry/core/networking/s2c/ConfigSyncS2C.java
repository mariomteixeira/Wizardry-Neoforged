package com.koomplo.wizardry.core.networking.s2c;

import com.koomplo.wizardry.WizardryMainMod;
import com.koomplo.wizardry.client.ClientPacketHandler;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

public record ConfigSyncS2C(String name, Map<String, JsonElement> configData) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ConfigSyncS2C> TYPE =
            new CustomPacketPayload.Type<>(WizardryMainMod.location("config_sync"));

    public static final StreamCodec<FriendlyByteBuf, ConfigSyncS2C> STREAM_CODEC =
            StreamCodec.of(ConfigSyncS2C::write, ConfigSyncS2C::read);

    private static void write(FriendlyByteBuf buf, ConfigSyncS2C packet) {
        buf.writeUtf(packet.name);
        buf.writeInt(packet.configData.size());
        for (Map.Entry<String, JsonElement> entry : packet.configData.entrySet()) {
            buf.writeUtf(entry.getKey());
            buf.writeUtf(entry.getValue().toString());
        }
    }

    private static ConfigSyncS2C read(FriendlyByteBuf buf) {
        String name = buf.readUtf();
        int size = buf.readInt();
        Map<String, JsonElement> configData = new HashMap<>();
        for (int i = 0; i < size; i++) {
            String key = buf.readUtf();
            String jsonString = buf.readUtf();
            JsonElement element = JsonParser.parseString(jsonString);
            configData.put(key, element);
        }
        return new ConfigSyncS2C(name, configData);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final ConfigSyncS2C packet, final IPayloadContext context) {
        context.enqueueWork(() -> ClientPacketHandler.handleConfigSync(packet));
    }

    public String getName() {
        return name;
    }

    public Map<String, JsonElement> getConfigData() {
        return configData;
    }
}
