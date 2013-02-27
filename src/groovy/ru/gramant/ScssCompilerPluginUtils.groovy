/*
 * ScssCompilerPluginUtils
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package ru.gramant

class ScssCompilerPluginUtils {

    static getPluginsConfig(config) {
        return config.plugin.grailsSassMinePlugin
    }

    static boolean pathContains(String path, String pathToCheck) {
        return path.contains(pathToCheck) //todo change
    }

}

