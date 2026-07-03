package com.binaris.wizardry.core.platform;

import com.binaris.wizardry.core.platform.services.INetworkHelper;
import com.binaris.wizardry.core.platform.services.IObjectData;
import com.binaris.wizardry.core.platform.services.IPlatformHelper;
import com.binaris.wizardry.core.platform.services.IRegistryUtil;
import com.binaris.wizardry.platform.ForgeNetworkHelper;
import com.binaris.wizardry.platform.ForgeObjectData;
import com.binaris.wizardry.platform.ForgePlatformHelper;
import com.binaris.wizardry.platform.ForgeRegistryUtil;

public class Services {
    public static final IPlatformHelper PLATFORM = new ForgePlatformHelper();
    public static final IObjectData OBJECT_DATA = new ForgeObjectData();
    public static final INetworkHelper NETWORK_HELPER = new ForgeNetworkHelper();
    public static final IRegistryUtil REGISTRY_UTIL = new ForgeRegistryUtil();
}
