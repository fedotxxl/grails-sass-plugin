/*
 * AbsctractScssCompiler
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package ru.gramant
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.plugins.GrailsPluginUtils

class AbstractScssCompiler {

    protected compilePaths = []
    protected ConfigObject config
    protected GrailsApplication grailsApplication

    AbstractScssCompiler(GrailsApplication grailsApplication) {
        this.grailsApplication = grailsApplication
        this.config = ScssCompilerPluginUtils.getPluginsConfig(grailsApplication.config)
        refreshConfig()
    }

    void refreshConfig() {
        compilePaths = []
    }

    List<String> getScssCompilePaths() {
        if (!compilePaths) {
            def modules = config.modules ?: [:]

             modules.each {
                def plugin = it.key
                if (plugin) {
                    def folders = ScssCompilerPluginUtils.toList(it.value)
                    folders.each { folder ->
                        def file = getRealFile(folder, plugin)
                        if (file?.exists()) compilePaths << file.canonicalPath
                    }
                }
            }
        }

        return compilePaths
    }

    File getRealFile(String path, String plugin = null) {
        if (ScssCompilerPluginUtils.isResourcesMode(config)) {
            path = (plugin) ?  "/plugins/" + plugin + "/" + path : path
            return grailsApplication.parentContext.getResource(path)?.file
        } else {
            def pluginFolder = (plugin) ? GrailsPluginUtils.pluginBuildSettings.getPluginDirForName(plugin)?.file : null
            return (pluginFolder) ? new File(pluginFolder, "web-app/" + path) : null
        }
    }
}
