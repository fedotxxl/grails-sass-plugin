/*
 * ScssConfigHolder
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package ru.gramant

class ScssConfigHolder {

    static ConfigObject config

    static void readPluginsConfig(ConfigObject config) {
        this.config = config.plugin.grailsSassMinePlugin
    }

}
