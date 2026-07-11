package com.koomplo.wizardry.client;

import com.koomplo.wizardry.WizardryMainMod;
import com.koomplo.wizardry.api.content.event.EBClientTickEvent;
import com.koomplo.wizardry.content.spell.necromancy.Possession;
import com.koomplo.wizardry.setup.registries.EBMobEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;

/**
 * Liga/desliga os post-shaders do 1.12.2 conforme o estado do player local.
 * Os programas phosphor/deconverge foram removidos do vanilla moderno, então são
 * shipados sob o namespace ebwizardry (GLSL do client 1.12.2 em #version 150).
 */
public final class PostEffects {

    public static final ResourceLocation POSSESSION = WizardryMainMod.location("shaders/post/possession.json");
    public static final ResourceLocation SIXTH_SENSE = WizardryMainMod.location("shaders/post/sixth_sense.json");
    public static final ResourceLocation SLOW_TIME = WizardryMainMod.location("shaders/post/slow_time.json");
    public static final ResourceLocation TRANSIENCE = WizardryMainMod.location("shaders/post/transience.json");

    private static ResourceLocation active;

    public static void onClientTick(EBClientTickEvent event) {
        Minecraft mc = event.getMinecraft();
        GameRenderer renderer = mc.gameRenderer;
        ResourceLocation desired = desiredShader(mc);

        if (desired == null) {
            if (active != null) {
                if (isOurs(renderer)) renderer.shutdownEffect();
                // 1.12.2: fim do shader de sixth sense/transience dispara o flash de blink
                if (SIXTH_SENSE.equals(active) || TRANSIENCE.equals(active)) ScreenOverlays.playBlinkEffect();
                active = null;
            }
        } else if (!desired.equals(active) || !isOurs(renderer)) {
            // Não atropela shaders de terceiros nem os de espectador (creeper/spider/invert)
            if (renderer.currentEffect() == null || isOurs(renderer)) {
                boolean firstLoad = active == null;
                renderer.loadEffect(desired);
                active = desired;
                // 1.12.2: sixth sense/transience/possession também piscam ao LIGAR (cast das spells)
                if (firstLoad && !SLOW_TIME.equals(desired)) ScreenOverlays.playBlinkEffect();
            }
        }
    }

    private static ResourceLocation desiredShader(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null || mc.getCameraEntity() != player) return null;
        if (Possession.isPossessing(player)) return POSSESSION;
        if (player.hasEffect(EBMobEffects.holder(EBMobEffects.SIXTH_SENSE))) return SIXTH_SENSE;
        if (player.hasEffect(EBMobEffects.holder(EBMobEffects.SLOW_TIME))) return SLOW_TIME;
        if (player.hasEffect(EBMobEffects.holder(EBMobEffects.TRANSIENCE))) return TRANSIENCE;
        return null;
    }

    private static boolean isOurs(GameRenderer renderer) {
        PostChain current = renderer.currentEffect();
        return current != null && current.getName().startsWith(WizardryMainMod.MOD_ID + ":");
    }

    private PostEffects() {
    }
}
