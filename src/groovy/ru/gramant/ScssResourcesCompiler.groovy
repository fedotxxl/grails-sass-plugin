/*
 * ScssResourcesCompiler
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package ru.gramant

import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.grails.plugin.resource.ResourceProcessor
import org.grails.plugin.resource.ResourceTagLib
import org.grails.plugin.resource.CSSPreprocessorResourceMapper
import org.grails.plugin.resource.CSSRewriterResourceMapper

@Slf4j
class ScssResourcesCompiler {

    private ConfigObject config
    private ScssDependentProcessor dependentProcessor
    private String sourceFolder
    private String projectPath

    ScssResourcesCompiler(ConfigObject config) {
        this.config = config
        this.dependentProcessor = new ScssDependentProcessor()
        this.projectPath = new File('.').canonicalPath

        refreshConfig()
    }

    void refreshConfig() {
        this.sourceFolder = FilenameUtils.normalize(config.resources.modules.folder.source)
    }

    void calculateDependentFiles(List<File> files) {
        def filteredFiles = files.findAll { needToProcess(it) }

        log.debug "SCSS: refreshing dependencies for files ${filteredFiles}"

        filteredFiles.each {
            dependentProcessor.refreshScssFile(it)
        }
    }

    void checkFileAndCompileDependents(File sourceFile) {
        if (needToProcess(sourceFile)) {
            log.trace "SCSS: refreshing dependencies for file [${sourceFile}]"
            dependentProcessor.refreshScssFile(sourceFile)

            def files = dependentProcessor.getDependentFiles(sourceFile)
            if (files) {
                log.debug "SCSS: touching dependent on [${sourceFile.name}] files ${files} to be recompiled by Resources plugin"

                files.each { file ->
                    touchFileToTriggerResourcePlugin(file)
                }
            } else {
                log.debug "SCSS: there is no dependent on [${sourceFile}] files"
            }
        }
    }

    void setupResourcesCompileSettings() {
        CSSPreprocessorResourceMapper.defaultIncludes.add('**/*.scss')
        CSSRewriterResourceMapper.defaultIncludes.add('**/*.scss')

        ResourceProcessor.DEFAULT_MODULE_SETTINGS['scss'] = [disposition: 'head'  ]
        ResourceTagLib.SUPPORTED_TYPES['scss'] = [
                type: "text/css",
                rel: 'stylesheet',
                media: 'screen, projection'
        ]
    }

    private touchFileToTriggerResourcePlugin(File file) {
        FileUtils.touch(file)
    }

    private boolean needToProcess(File file) {
        def path = file.canonicalPath

        if (ScssCompilerPluginUtils.pathContains(path, projectPath)) {
            return true
        } else if (sourceFolder && ScssCompilerPluginUtils.pathContains(path, sourceFolder)) {
            return true
        } else {
            return false
        }
    }
}
