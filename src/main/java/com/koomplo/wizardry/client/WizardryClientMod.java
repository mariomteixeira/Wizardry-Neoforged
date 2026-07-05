package com.koomplo.wizardry.client;

import com.koomplo.wizardry.core.config.ConfigManager;
import com.koomplo.wizardry.core.config.EBClientConfig;

public final class WizardryClientMod {

    public static void init() {
        ConfigManager.register(EBClientConfig.INSTANCE);
    }

    private WizardryClientMod() {
    }
}
