/*
 * ScssResourcesCompiler
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package ru.gramant
import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.grails.plugin.resource.ResourceProcessor
import org.grails.plugin.resource.ResourceTagLib
import org.grails.plugin.resource.CSSPreprocessorResourceMapper
import org.grails.plugin.resource.CSSRewriterResourceMapper
import static ru.gramant.ScssCompilerPluginUtils.path
import static ru.gramant.ScssCompilerPluginUtils.paths

@Slf4j
class ScssResourcesCompiler extends AbstractScssCompiler {

    private ScssDependentProcessor dependentProcessor

    ScssResourcesCompiler(GrailsApplication grailsApplication) {
        super(grailsApplication)
        this.dependentProcessor = new ScssDependentProcessor()
    }

    void calculateDependentFiles(List<File> files) {
        log.debug "SCSS: refreshing dependencies for files ${paths(files)}"

        files.each {
            dependentProcessor.refreshScssFile(it)
        }
    }

    void checkFileAndCompileDependents(File sourceFile) {
        log.debug "Checking file [${path(sourceFile)}] and compile dependent on it files"

        //update dependencies for changed file
        dependentProcessor.refreshScssFile(sourceFile)

        def files = dependentProcessor.getDependentFiles(sourceFile)
        if (files) {
            log.debug "SCSS: touching dependent on [${path(sourceFile)}] files ${paths(files)} to be recompiled by Resources plugin"

            files.each { file ->
                touchFileToTriggerResourcePlugin(file)
            }
        } else {
            log.debug "SCSS: there is no dependent on [${path(sourceFile)}] files"
        }
    }

    void setupResourcesCompileSettings() {
        log.debug "SCSS: register resources compile settings"

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
