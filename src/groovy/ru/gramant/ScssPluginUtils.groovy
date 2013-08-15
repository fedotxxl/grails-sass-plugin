/*
 * ScssPluginUtils
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package ru.gramant

import org.apache.commons.io.FileUtils
import org.codehaus.groovy.grails.plugins.GrailsPluginUtils

class ScssPluginUtils {

    private static final PROJECT_FOLDER = new File('.')

    static boolean isProjectOrInlinePluginFile(File file) {
        def isProjectFile = FileUtils.directoryContains(PROJECT_FOLDER, file)
        def isInlinePluginFile = {
            return GrailsPluginUtils.pluginBuildSettings.getInlinePluginDirectories().any { FileUtils.directoryContains(it.file, file) }
        }

        return isProjectFile || isInlinePluginFile()
    }

}
