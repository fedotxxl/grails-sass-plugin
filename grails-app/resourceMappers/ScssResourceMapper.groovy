import org.codehaus.groovy.grails.plugins.GrailsPlugin
import org.grails.plugin.resource.ResourceMeta
import org.grails.plugin.resource.mapper.MapperPhase
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ru.gramant.ScssCompilerPluginUtils
import ru.gramant.ScssConfigHolder
import ru.gramant.ScssUtils
import static ru.gramant.ScssCompilerPluginUtils.path

class ScssResourceMapper {

    private static final Logger log = LoggerFactory.getLogger("ru.gramant.ScssResourceMapper")

    def grailsApplication
    def pluginManager

    def cache = [:]

    static phase = MapperPhase.GENERATION
    static defaultIncludes = ['**/*.scss', '**/*.sass']

    def map(ResourceMeta resource, c) {
        if (ScssCompilerPluginUtils.isResourcesMode()) {
            log.trace "SCSS: processing SCSS resource: ${resource}"

            try {
                File scssFile = resource.processedFile
                if (resource.originalUrl && ScssCompilerPluginUtils.isScssFile(scssFile)) {
                    def cssFile = new File("${scssFile.absolutePath}.css")

                    log.debug "SCSS: Compiling SCSS file [${path(scssFile)}] into [${path(cssFile)}]"

                    def realFile = getRealFile(resource.originalUrl)
                    def compiled = getFromCache(realFile)

                    if (compiled) {
                        log.debug "SCSS: for file ${path(scssFile)} use data from cache"
                    } else {
                        def paths = getScssCompilePaths()
                        compiled = ScssUtils.instance.compile(realFile, paths, ScssConfigHolder.config.compass, ScssConfigHolder.config)
                    }

                    if (compiled != null) {
                        cssFile.write(compiled, "UTF-8")

                        resource.processedFile = cssFile
                        resource.contentType = 'text/css'
    //                  resource.sourceUrlExtension = 'css'
    //                  resource.tagAttributes.rel = 'stylesheet'
                        resource.actualUrl = "${resource.originalUrl}.css"

                        addToCache(realFile, compiled)

                        log.info "SCSS: overwrite ${resource.originalUrl}"
                    } else {
                        processCompilationFailure(resource)
                    }
                } else {
                    log.debug("SCSS: skipped file [${path(scssFile)}]")
                }
            } catch (e) {
                processCompilationFailure(resource)
                log.error("SCSS: Exception while parsing file [${path(resource.processedFile)}]", e)
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

    List<String> getScssCompilePaths() {
        def answer = []
        def path = ScssConfigHolder.config.resources.modules.folder.source

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

    private processCompilationFailure(ResourceMeta resource) {
        if (ScssConfigHolder.config.resources.exceptionOnFailedCompilation) {
            resource.processedFile = new File('non_existing_file.css')
            resource.updateExists()
        } else {
            resource.actualUrl = "${resource.originalUrl}.FAILED.scss"
        }
    }
}
