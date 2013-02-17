import org.grails.plugin.resource.mapper.MapperPhase
import grails.util.GrailsUtil
import ru.gramant.ScssUtils
import org.slf4j.LoggerFactory

class ScssResourceMapper {

    private static final LOG = LoggerFactory.getLogger(this)

    def grailsApplication
    def phase = MapperPhase.GENERATION

    static defaultIncludes = ['**/*.scss', '**/*.sass']
    private static SASS_FILE_EXTENSIONS = ['.scss', '.sass']

    def map(resource, config) {
        try {
            println "processing file ${resource.processedFile}"
            File originalFile = resource.processedFile

            if (resource.originalUrl && isSassFile(originalFile)) {
                println "sass: compiling"

                def source = originalFile.text
                def path = grailsApplication.parentContext.getResource(resource.originalUrl)?.file?.parentFile?.absolutePath

                def compiled = ScssUtils.compile(
                        grailsApplication,
                        source,
                        path)

                def file = new File("${originalFile.absolutePath}.css")
                file.write(compiled, "UTF-8")

                resource.processedFile = file
//                resource.contentType = 'text/css'
//                resource.sourceUrlExtension = 'css'
//                resource.tagAttributes.rel = 'stylesheet'
                resource.actualUrl = "${resource.originalUrl}.css"
                resource.updateActualUrlFromProcessedFile()
            } else {
                println "sass: skipped"
            }
        } catch (e) {
            LOG.error("Exception while parsing file ${resource.processedFile}", e)
            e.printStackTrace()
        }
    }

    private boolean isSassFile(File file) {
        for (def extension in SASS_FILE_EXTENSIONS) {
            if (file.name.toLowerCase().endsWith(extension)) {
                return true
            }
        }

        return false
    }


    def likeLess(resource) {
        File lessFile = resource.processedFile
        File cssFile = new File(lessFile.absolutePath + '.css')

        def importPath = grailsApplication.parentContext.getResource(resource.originalUrl)?.file?.parentFile?.absolutePath
        if (importPath) {
            def order = resource.tagAttributes.order ?: 10
            log.debug "Adding import path [${importPath}][order: ${order}] for resource [${resource}]"
            paths << [path:importPath, order:order]
            paths.sort {it.order}
        }

        try {  sass
            log.debug "Compiling LESS file [${lessFile}] into [${cssFile}]"
            lessCompilerService.compile (lessFile, cssFile, paths.collect {it.path})
            resource.processedFile = cssFile
            resource.contentType = 'text/css'
            resource.tagAttributes.rel = 'stylesheet'
            resource.updateActualUrlFromProcessedFile()
        } catch (Exception e) {
            log.error("Error compiling less file: ${lessFile}", e)
        }
    }
}
