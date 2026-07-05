package com.koomplo.wizardry.core.platform;

import com.koomplo.wizardry.core.platform.services.INetworkHelper;
import com.koomplo.wizardry.core.platform.services.IObjectData;
import com.koomplo.wizardry.core.platform.services.IPlatformHelper;
import com.koomplo.wizardry.core.platform.services.IRegistryUtil;
import com.koomplo.wizardry.platform.ForgeNetworkHelper;
import com.koomplo.wizardry.platform.ForgeObjectData;
import com.koomplo.wizardry.platform.ForgePlatformHelper;
import com.koomplo.wizardry.platform.ForgeRegistryUtil;

public class Services {
    public static final IPlatformHelper PLATFORM = new ForgePlatformHelper();
    public static final IObjectData OBJECT_DATA = new ForgeObjectData();
    public static final INetworkHelper NETWORK_HELPER = new ForgeNetworkHelper();
    public static final IRegistryUtil REGISTRY_UTIL = new ForgeRegistryUtil();
}
