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

                def c = new HashMap(grailsApplication.config.grails.sass.flatten())
                def source = originalFile.text

                def compiled = ScssUtils.compile(
                        source,
                        resource.originalResource.file.parent,
                        c.syntax,
                        c.style,
                        c.debugInfo as Boolean,
                        c.lineComments as Boolean)

                def file = new File("${originalFile.absolutePath}.css")
                file.write(compiled, "UTF-8")

                resource.processedFile = file
                resource.contentType = 'text/css'
                resource.sourceUrlExtension = 'css'
                resource.tagAttributes.rel = 'stylesheet'
                resource.actualUrl = "${resource.originalUrl}.css"
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

}
