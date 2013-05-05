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
    private compilePathTotal = [] as Set

    private ScssCompilePathProcessor() {
    }

    void init(Collection<File> folders) {
        folders.each { folder ->
            compilePathTotal << folder.canonicalPath
        }

        refreshConfig()
    }

    void refreshConfig() {
        compilePathExclude.clear()
        ScssConfigHolder.config.compilePathExclude?.each { path ->
            compilePathExclude << FilenameUtils.separatorsToUnix(path)
        }

        //update compile path
        compilePath.clear()
        compilePathTotal.each { path ->
            checkAndAddToCompilePath(path)
        }

        log.debug "SCSS: calculated compile path: ${paths(compilePath)}"
    }

    void addFolderToCompilePath(File file) {
        def path = file.canonicalPath

        compilePathTotal << path
        checkAndAddToCompilePath(path)
    }

    def getCompilePath() {
        return compilePath
    }

    private checkAndAddToCompilePath(String canonicalPath) {
        if (!isShouldBeExcluded(canonicalPath)) {
            compilePath << canonicalPath
        }
    }

    private isShouldBeExcluded(String canonicalPath) {
        def path = FilenameUtils.separatorsToUnix(canonicalPath)

        for (String excludePath : compilePathExclude) {
            if (excludePath && path.endsWith(excludePath)) return true
        }

        return false
    }
}
