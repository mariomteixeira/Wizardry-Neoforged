package com.koomplo.wizardry.client;

import com.koomplo.wizardry.WizardryMainMod;
import com.koomplo.wizardry.api.content.event.EBClientTickEvent;
import com.koomplo.wizardry.setup.registries.EBMobEffects;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;

/**
 * Overlays de tela do 1.12.2: sixth sense e frost (contínuos, enquanto o efeito durar)
 * e o flash de blink (8 ticks, blend aditivo + FOV) usado por blink/phase step/banish
 * e pelo fim dos shaders de sixth sense/transience.
 */
public final class ScreenOverlays {

    private static final ResourceLocation SIXTH_SENSE_TEXTURE = WizardryMainMod.location("textures/gui/sixth_sense_overlay.png");
    private static final ResourceLocation FROST_TEXTURE = WizardryMainMod.location("textures/gui/frost_overlay.png");
    private static final ResourceLocation BLINK_TEXTURE = WizardryMainMod.location("textures/gui/blink_overlay.png");

    /** Duração do flash de blink em ticks (1.12.2 RenderBlinkEffect). */
    private static final int BLINK_EFFECT_DURATION = 8;
    private static int blinkTimer;

    /** Inicia o flash de blink se o player dado for o player local (1.12.2 ClientProxy#playBlinkEffect). */
    public static void playBlinkEffect(Player player) {
        if (Minecraft.getInstance().player == player) playBlinkEffect();
    }

    public static void playBlinkEffect() {
        blinkTimer = BLINK_EFFECT_DURATION;
    }

    public static void onClientTick(EBClientTickEvent event) {
        if (blinkTimer > 0) blinkTimer--;
    }

    public static void render(GuiGraphics guiGraphics) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        if (player.hasEffect(EBMobEffects.holder(EBMobEffects.SIXTH_SENSE))) {
            drawOverlay(guiGraphics, SIXTH_SENSE_TEXTURE, 1f, false);
        }
        if (player.hasEffect(EBMobEffects.holder(EBMobEffects.FROST))) {
            drawOverlay(guiGraphics, FROST_TEXTURE, 1f, false);
        }
        if (blinkTimer > 0) {
            drawOverlay(guiGraphics, BLINK_TEXTURE, (float) blinkTimer / BLINK_EFFECT_DURATION, true);
        }
    }

    private static void drawOverlay(GuiGraphics guiGraphics, ResourceLocation texture, float alpha, boolean additive) {
        RenderSystem.enableBlend();
        if (additive) {
            // Blend do blink no 1.12.2: (SRC_ALPHA, ONE, ONE, ZERO)
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                    GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        }
        guiGraphics.setColor(1, 1, 1, alpha);
        guiGraphics.blit(texture, 0, 0, guiGraphics.guiWidth(), guiGraphics.guiHeight(), 0, 0, 256, 256, 256, 256);
        guiGraphics.setColor(1, 1, 1, 1);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    public static void onComputeFovModifier(ComputeFovModifierEvent event) {
        if (blinkTimer > 0 && event.getPlayer() == Minecraft.getInstance().player) {
            float f = (float) Math.max(blinkTimer - 2, 0) / BLINK_EFFECT_DURATION;
            event.setNewFovModifier(event.getNewFovModifier() + f * f * 0.7f);
        }
    }

    private ScreenOverlays() {
    }
}
