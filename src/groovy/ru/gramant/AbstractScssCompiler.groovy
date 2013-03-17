/*
 * AbsctractScssCompiler
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package ru.gramant

import org.apache.commons.io.FileUtils
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.plugins.GrailsPluginInfo
import org.codehaus.groovy.grails.plugins.GrailsPluginUtils

class AbstractScssCompiler {

    protected compilePaths = []
    protected ConfigObject config
    protected GrailsApplication grailsApplication
    protected File projectFolder

    AbstractScssCompiler(GrailsApplication grailsApplication) {
        this.grailsApplication = grailsApplication
        this.config = ScssCompilerPluginUtils.getPluginsConfig(grailsApplication.config)
        this.projectFolder = new File('.')
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

    /**
     * Checks whether plugin is inline.
     * https://github.com/grails/grails-core/blob/e8793e97fd0126d78d2fc3c1c548a7690be91160/grails-bootstrap/src/main/groovy/org/codehaus/groovy/grails/resolve/PluginInstallEngine.groovy
     * @todo most probably it is required to search for plugin not just by name but also using its goupdId
     * @param name The plugin name
     * @return true iff plugin is inline one
     */
    protected boolean isInlinePlugin(String name) {
        GrailsPluginInfo info = GrailsPluginUtils.pluginBuildSettings.getPluginInfoForName(name);
        return (info != null) && GrailsPluginUtils.pluginBuildSettings.getInlinePluginDirectories().find {it == info.getPluginDir()}
    }

    protected boolean isProjectOrInlinePluginFile(File file) {
        def isProjectFile = FileUtils.directoryContains(projectFolder, file)
        def isInlinePluginFile = {
            return GrailsPluginUtils.pluginBuildSettings.getInlinePluginDirectories().any { FileUtils.directoryContains(it.file, file) }
        }

        return isProjectFile || isInlinePluginFile()
    }
}
