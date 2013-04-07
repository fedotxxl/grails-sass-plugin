/*
 * ScssCompilerPluginUtils
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package ru.gramant

import org.apache.commons.io.FilenameUtils

class ScssCompilerPluginUtils {

    private static SCSS_FILE_EXTENSIONS = ['.scss', '.sass']

    static Boolean isResourcesMode() {
        return ScssConfigHolder.config.mode == 'resources'
    }

    static Boolean isDiskMode() {
        return !isResourcesMode()
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

    static String relativeToProjectPath(File file) {
        def basePath = FilenameUtils.separatorsToUnix(FilenameUtils.normalize(System.properties['base.dir']))
        def filePath = FilenameUtils.separatorsToUnix(FilenameUtils.normalize(file.canonicalPath))

        if (basePath && filePath.startsWith(basePath)) {
            return "/" + filePath.substring(basePath.length()+1)
        } else {
            return filePath
        }
    }

    static List<String> relativeToProjectPath(Collection<File> files) {
        return files.collect { relativeToProjectPath(it) }
    }

    static String path(File file) {
        if (ScssConfigHolder.config.relativePaths) {
            return relativeToProjectPath(file)
        } else {
            return file.canonicalPath
        }
    }

    static String paths(Collection<File> files) {
        if (ScssConfigHolder.config.relativePaths) {
            return relativeToProjectPath(files)
        } else {
            return files.collect {it.canonicalPath}
        }
    }
}

