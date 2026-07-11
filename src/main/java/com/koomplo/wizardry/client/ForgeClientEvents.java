package com.koomplo.wizardry.client;

import com.koomplo.wizardry.WizardryMainMod;
import com.koomplo.wizardry.client.effect.ArcaneLockRender;
import com.koomplo.wizardry.client.effect.ContainmentFieldRender;
import com.koomplo.wizardry.core.config.ConfigManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = WizardryMainMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class ForgeClientEvents {

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        ContainmentFieldRender.render(event.getCamera(), event.getPoseStack(), partialTick);
        ArcaneLockRender.render(event.getCamera(), event.getPoseStack(), partialTick);
    }

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        ConfigManager.restoreLocalConfigs();
    }

    @SubscribeEvent
    public static void onComputeFovModifier(ComputeFovModifierEvent event) {
        ScreenOverlays.onComputeFovModifier(event);
    }
}
