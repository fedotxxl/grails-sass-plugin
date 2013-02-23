import groovy.util.logging.Slf4j
import org.grails.plugin.resource.ResourceMeta
import org.grails.plugin.resource.mapper.MapperPhase
import grails.util.GrailsUtil
import ru.gramant.ScssCompilerPluginUtils
import ru.gramant.ScssUtils
import org.slf4j.LoggerFactory

@Slf4j
class ScssResourceMapper {

    def grailsApplication
    def phase = MapperPhase.GENERATION

    static defaultIncludes = ['**/*.scss', '**/*.sass']
    private static SCSS_FILE_EXTENSIONS = ['.scss', '.sass']

    def map(ResourceMeta resource, c) {
        ConfigObject config = ScssCompilerPluginUtils.getPluginsConfig(grailsApplication.config)
        if (config.resourcesMode) {
            try {
                File scssFile = resource.processedFile
                if (resource.originalUrl && isScssFile(scssFile)) {
                    def cssFile = new File("${scssFile.absolutePath}.css")

                    log.debug "SCSS: Compiling SCSS file [${scssFile}] into [${cssFile}]"

                    def path = grailsApplication.parentContext.getResource(resource.originalUrl)?.file?.parentFile?.absolutePath
                    def compiled = ScssUtils.compile(scssFile, path, config)
                    if (compiled != null) {
                        cssFile.write(compiled, "UTF-8")

                        resource.processedFile = cssFile
                        resource.contentType = 'text/css'
    //                  resource.sourceUrlExtension = 'css'
    //                  resource.tagAttributes.rel = 'stylesheet'
                        resource.actualUrl = "${resource.originalUrl}.css"
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

    private processCompilationFailure(ResourceMeta resource, ConfigObject config) {
        if (config.resources.exceptionOnFailedCompilation) {
            resource.processedFile = new File('non_existing_file.css')
            resource.updateExists()
        } else {
            resource.actualUrl = "${resource.originalUrl}.FAILED.scss"
        }
    }

    private boolean isScssFile(File file) {
        for (def extension in SCSS_FILE_EXTENSIONS) {
            if (file.name.toLowerCase().endsWith(extension)) {
                return true
            }
        }

        return false
    }
}
