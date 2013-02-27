/*
 * ScssCompilerPluginUtils
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package ru.gramant

class ScssCompilerPluginUtils {

    private static SCSS_FILE_EXTENSIONS = ['.scss', '.sass']

    static getPluginsConfig(config) {
        return config.plugin.grailsSassMinePlugin
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

}

