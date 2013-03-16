/*
 * ScssCompilerPluginUtils
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package ru.gramant

class ScssCompilerPluginUtils {

    private static SCSS_FILE_EXTENSIONS = ['.scss', '.sass']

    static ConfigObject getPluginsConfig(ConfigObject config) {
        return config.plugin.grailsSassMinePlugin
    }

    static Boolean isResourcesMode(ConfigObject config) {
        return config.mode == 'resources'
    }

    static Boolean isDiskMode(ConfigObject config) {
        return !isResourcesMode(config)
    }

    static boolean pathContains(String path, String pathToCheck) {
        return path.contains(pathToCheck) //todo change
    }

    static boolean isScssFile(File file) {
        for (def extension in SCSS_FILE_EXTENSIONS) {
            if (file.name.toLowerCase().endsWith(extension)) {
                return true
            }
        }

        return false
    }

    static List toList(value) {
        [value].flatten().findAll { it != null }
    }

}

