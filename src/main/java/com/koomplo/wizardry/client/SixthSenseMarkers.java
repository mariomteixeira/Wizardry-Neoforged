package com.koomplo.wizardry.client;

import com.koomplo.wizardry.WizardryMainMod;
import com.koomplo.wizardry.content.spell.DefaultProperties;
import com.koomplo.wizardry.core.config.EBServerConfig;
import com.koomplo.wizardry.setup.registries.EBMobEffects;
import com.koomplo.wizardry.setup.registries.Spells;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import org.joml.Matrix4f;

/**
 * Marcadores do sixth sense sobre criaturas próximas, visíveis através de paredes
 * (1.12.2 RenderSixthSense). Só o marcador passivo é usado — as variantes hostile/player
 * eram destravadas pelo charm_sixth_sense, artefato fora do escopo do port.
 */
@EventBusSubscriber(modid = WizardryMainMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class SixthSenseMarkers extends RenderType {

    private static final ResourceLocation MARKER_TEXTURE = WizardryMainMod.location("textures/gui/sixth_sense_marker_passive.png");

    private static final RenderType MARKER = create("ebwizardry_sixth_sense_marker",
            DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 256, false, true,
            CompositeState.builder()
                    .setShaderState(new ShaderStateShard(GameRenderer::getPositionTexColorShader))
                    .setTextureState(new TextureStateShard(MARKER_TEXTURE, false, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(NO_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false));

    @SubscribeEvent
    public static void onRenderLiving(RenderLivingEvent.Post<?, ?> event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        LivingEntity entity = event.getEntity();

        if (player == null || entity == player || entity instanceof ArmorStand) return;

        MobEffectInstance effect = player.getEffect(EBMobEffects.holder(EBMobEffects.SIXTH_SENSE));
        if (effect == null) return;

        // Amplifier codifica os upgrades de range (mesma fórmula do SixthSense#cast)
        float effectRadius = Spells.SIXTH_SENSE.property(DefaultProperties.EFFECT_RADIUS);
        float distance = entity.distanceTo(player);
        if (distance >= effectRadius * (1 + effect.getAmplifier() * EBServerConfig.RANGE_INCREASE_PER_LEVEL.get())) return;

        // Opacidade cai depois de 80% do raio base (1.12.2)
        float alpha = Math.min(1f, 5f * (1f - distance / effectRadius));
        if (alpha <= 0) return;

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(0, entity.getBbHeight() * 0.6, 0);
        poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());

        Matrix4f pose = poseStack.last().pose();
        VertexConsumer buffer = event.getMultiBufferSource().getBuffer(MARKER);
        int a = (int) (alpha * 255);
        buffer.addVertex(pose, -0.6f, 0.6f, 0).setUv(0, 0).setColor(255, 255, 255, a);
        buffer.addVertex(pose, -0.6f, -0.6f, 0).setUv(0, 1).setColor(255, 255, 255, a);
        buffer.addVertex(pose, 0.6f, -0.6f, 0).setUv(1, 1).setColor(255, 255, 255, a);
        buffer.addVertex(pose, 0.6f, 0.6f, 0).setUv(1, 0).setColor(255, 255, 255, a);

        poseStack.popPose();
    }

    private SixthSenseMarkers(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize,
                              boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }
}
