package com.koomplo.wizardry.client.renderer.entity;

import com.koomplo.wizardry.content.entity.living.DecoyEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Renders the decoy with the caster's own skin on a standard player model, so it passes for the player
 * (the 1.12.2 renderer hijacked the caster's renderer instead; using the skin is equivalent and much simpler).
 */
public class DecoyRenderer extends MobRenderer<DecoyEntity, PlayerModel<DecoyEntity>> {

    public DecoyRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5f);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull DecoyEntity entity) {
        UUID casterUUID = entity.getCasterUUID();

        if (casterUUID != null) {
            ClientPacketListener connection = Minecraft.getInstance().getConnection();
            if (connection != null) {
                PlayerInfo info = connection.getPlayerInfo(casterUUID);
                if (info != null) return info.getSkin().texture();
            }
            return DefaultPlayerSkin.get(casterUUID).texture();
        }

        return DefaultPlayerSkin.getDefaultTexture();
    }
}
