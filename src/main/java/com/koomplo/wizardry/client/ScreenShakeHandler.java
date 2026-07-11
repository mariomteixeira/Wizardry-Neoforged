package com.koomplo.wizardry.client;

import com.koomplo.wizardry.api.content.event.EBClientTickEvent;
import net.minecraft.client.player.LocalPlayer;

/**
 * Tremor de tela do 1.12.2: pitch alterna de lado a cada tick com magnitude decaindo
 * (counter * SHAKINESS), começando meio pitch para baixo.
 */
public final class ScreenShakeHandler {
    private static final float SHAKINESS = 0.5f;
    private static int screenShakeCounter = 0;

    public static void onClientTick(EBClientTickEvent event) {
        LocalPlayer player = event.getMinecraft().player;
        if (player == null) {
            screenShakeCounter = 0;
            return;
        }
        if (screenShakeCounter > 0) {
            float magnitude = screenShakeCounter * SHAKINESS;
            player.setXRot(player.getXRot() + (screenShakeCounter % 2 == 0 ? magnitude : -magnitude));
            screenShakeCounter--;
        }
    }

    public static void shakeScreen(float intensity) {
        screenShakeCounter = (int) (intensity / SHAKINESS);
        LocalPlayer player = net.minecraft.client.Minecraft.getInstance().player;
        if (player != null) player.setXRot(player.getXRot() - intensity * 0.5f);
    }

    private ScreenShakeHandler() {
    }
}
