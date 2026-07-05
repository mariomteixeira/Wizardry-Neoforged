package com.koomplo.wizardry.client;

import com.koomplo.wizardry.api.content.event.EBClientTickEvent;
import com.koomplo.wizardry.api.content.event.EBEntityJoinLevelEvent;
import com.koomplo.wizardry.api.content.event.EBLivingTick;
import com.koomplo.wizardry.client.effect.ArcaneLockRender;
import com.koomplo.wizardry.client.sound.SoundLoop;
import com.koomplo.wizardry.core.event.WizardryEventBus;
import com.koomplo.wizardry.setup.registries.client.EBKeyBinding;

public final class EBClientEventHelper {
    private EBClientEventHelper() {
    }

    public static void register() {
        WizardryEventBus bus = WizardryEventBus.getInstance();
        onLivingTickEvent(bus);
        onClientTick(bus);
        onJoin(bus);
    }

    private static void onLivingTickEvent(WizardryEventBus bus) {
        bus.register(EBLivingTick.class, SpellGUIDisplay::onLivingTickEvent);
    }

    private static void onClientTick(WizardryEventBus bus) {
        bus.register(EBClientTickEvent.class, SoundLoop::onClientTick);
        bus.register(EBClientTickEvent.class, EBKeyBinding::onClientTick);
        bus.register(EBClientTickEvent.class, ScreenShakeHandler::onClientTick);
    }

    private static void onJoin(WizardryEventBus bus) {
        bus.register(EBEntityJoinLevelEvent.class, ArcaneLockRender::onJoin);
    }

}
