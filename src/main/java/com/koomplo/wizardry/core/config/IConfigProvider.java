package com.koomplo.wizardry.core.config;

import com.koomplo.wizardry.core.config.option.ConfigOption;

import java.util.List;

public interface IConfigProvider {
    List<ConfigOption<?>> getOptions();
}

