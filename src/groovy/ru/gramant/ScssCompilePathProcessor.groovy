/*
 * ScssCompilePathProcessor
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package ru.gramant

import groovy.util.logging.Slf4j
import org.apache.commons.io.FilenameUtils
import static ru.gramant.ScssCompilerPluginUtils.paths

@Slf4j
@Singleton
class ScssCompilePathProcessor {
    private compilePath = [] as Set
    private compilePathExclude = [] as Set

    private ScssCompilePathProcessor() {
        refreshConfig()
    }

    void refreshConfig() {
        compilePathExclude.clear()
        ScssConfigHolder.config.compilePathExclude?.each { path ->
            compilePathExclude << FilenameUtils.separatorsToUnix(path)
        }
    }

    void recalculateCompilePath(List files) {
        compilePath.clear()
        files.each { File file ->
            addFolderToCompilePath(file.parentFile)
        }

        log.debug "SCSS: calculated compile path: ${paths(compilePath)}"
    }

    void addFolderToCompilePath(File file) {
        if (!isShouldBeExcluded(file)) {
            compilePath << file.canonicalPath

        }
    }

    def getCompilePath() {
        return compilePath
    }

    private isShouldBeExcluded(File file) {
        def path = FilenameUtils.separatorsToUnix(file.canonicalPath)

        for (String excludePath : compilePathExclude) {
            if (path.endsWith(excludePath)) return true
        }

        return false
    }
}
