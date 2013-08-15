/*
 * ScssCompilerPluginUtils
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package ru.gramant

import org.apache.commons.io.FilenameUtils

class ScssCompilerPluginUtils {

    private static SCSS_FILE_EXTENSIONS = ['.scss', '.sass']

    private static List relativeProjectPaths = []

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
        if (!relativeProjectPaths) {
            def base = new File(System.properties['base.dir'])
            if (base) {
                relativeProjectPaths = [base.canonicalPath, base.parentFile.canonicalPath]
            }
        }

        def filePath = file.canonicalPath
        for (int i = 0; i < relativeProjectPaths.size(); i++) {
            def basePath = relativeProjectPaths[i]
            if (basePath && filePath.startsWith(basePath)) {
                def prefix = (i == 0) ? "/" : i.collect { "../" }.join("")
                return prefix + FilenameUtils.separatorsToUnix(filePath.substring(basePath.length()+1))
            }
        }

        return filePath
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

    static String path(String path) {
        return path(new File(path))
    }

    static String paths(Collection filesOrPaths) {

        def files = filesOrPaths.collect{
            if (it instanceof String) {
                return new File(it)
            } else if (it instanceof File) {
                return it
            } else {
                return null
            }
        }

        if (ScssConfigHolder.config.relativePaths) {
            return relativeToProjectPath(files)
        } else {
            return files.collect {it?.canonicalPath}
        }
    }
}

