/*
 * ScssResourcesCompiler
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package ru.gramant

import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils
import org.grails.plugin.resource.ResourceProcessor
import org.grails.plugin.resource.ResourceTagLib
import org.grails.plugin.resource.CSSPreprocessorResourceMapper
import org.grails.plugin.resource.CSSRewriterResourceMapper

@Slf4j
class ScssResourcesCompiler {

    private dependentProcessor

    ScssResourcesCompiler() {
        this.dependentProcessor = new ScssDependentProcessor()
    }

    void calculateDependentFiles(List<File> files) {
        log.debug "SCSS: refreshing dependencies for files ${files}"

        files.each {
            dependentProcessor.refreshScssFile(it)
        }
    }

    void checkFileAndCompileDependents(File sourceFile) {
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
}
