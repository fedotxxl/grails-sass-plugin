package ru.gramant

import groovy.util.logging.Slf4j
import org.codehaus.groovy.grails.plugins.GrailsPlugin
import org.grails.plugin.resource.ResourceMeta
import org.grails.plugin.resource.mapper.MapperPhase
import ru.gramant.ScssCompilerPluginUtils
import ru.gramant.ScssUtils

@Slf4j
class ScssResourceMapper {

    def grailsApplication
    def pluginManager
    def phase = MapperPhase.GENERATION
    def cache = [:]

    static defaultIncludes = ['**/*.scss', '**/*.sass']

    def map(ResourceMeta resource, c) {
        ConfigObject config = ScssCompilerPluginUtils.getPluginsConfig(grailsApplication.config)
        if (config.resourcesMode) {
            try {
                File scssFile = resource.processedFile
                if (resource.originalUrl && ScssCompilerPluginUtils.isScssFile(scssFile)) {
                    def cssFile = new File("${scssFile.absolutePath}.css")

                    log.debug "SCSS: Compiling SCSS file [${scssFile}] into [${cssFile}]"

                    def realFile = getRealFile(resource.originalUrl)
                    def compiled = getFromCache(realFile)

                    if (compiled) {
                        log.debug "SCSS: for file ${scssFile} use data from cache"
                    } else {
                        def paths = getScssCompilePaths(config)
                        compiled = ScssUtils.compile(realFile, paths, config.compass, config)
                    }

                    if (compiled != null) {
                        cssFile.write(compiled, "UTF-8")

                        resource.processedFile = cssFile
                        resource.contentType = 'text/css'
    //                  resource.sourceUrlExtension = 'css'
    //                  resource.tagAttributes.rel = 'stylesheet'
                        resource.actualUrl = "${resource.originalUrl}.css"

                        addToCache(realFile, compiled)
                    } else {
                        processCompilationFailure(resource, config)
                    }
                } else {
                    log.debug("SCSS: skipped file [${scssFile}]")
                }
            } catch (e) {
                processCompilationFailure(resource, config)
                log.error("SCSS: Exception while parsing file [${resource.processedFile}]", e)
            }
        }
    }

    private addToCache(File file, String css) {
        cache[file.canonicalPath] = [lastModified: file.lastModified(), css: css]
    }

    private getFromCache(File file) {
        def answer = null

        def data = cache[file.canonicalPath]
        if (data) {
            if (file.lastModified() == data.lastModified) answer = data.css
        }

        return answer
    }

    private getScssCompilePaths(ConfigObject config) {
        def answer = []
        def path = config.resources.modules.folder.source

        if (path) {
            pluginManager.userPlugins.each { plugin ->
                def file = getRealFile(path, plugin)
                if (file?.exists()) answer << file.canonicalPath
            }
        }

        return answer
    }

    private File getRealFile(String path, GrailsPlugin plugin = null) {
        path = (plugin) ?  "/plugins/" + plugin.fullName + "/" + path : path
        return grailsApplication.parentContext.getResource(path)?.file
    }

    private processCompilationFailure(ResourceMeta resource, ConfigObject config) {
        if (config.resources.exceptionOnFailedCompilation) {
            resource.processedFile = new File('non_existing_file.css')
            resource.updateExists()
        } else {
            resource.actualUrl = "${resource.originalUrl}.FAILED.scss"
        }
    }


}
