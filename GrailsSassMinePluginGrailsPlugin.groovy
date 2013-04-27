import groovy.util.logging.Slf4j
import org.springframework.core.io.FileSystemResource
import ru.gramant.ScssCompilerPluginUtils as PluginUtils
import ru.gramant.ScssConfigHolder
import ru.gramant.ScssDiskCompiler
import ru.gramant.ScssResourcesCompiler
import static ru.gramant.ScssCompilerPluginUtils.path

@Slf4j
class GrailsSassMinePluginGrailsPlugin {
    // the plugin version
    def version = "0.1.7.22"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.0 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/conf/SassResources.groovy",
            "grails-app/conf/UrlMappings.groovy",
            "web-app/*"
    ]

    // TODO Fill in these fields
    def title = "Grails Sass Mine Plugin Plugin" // Headline display name of the plugin
    def author = "Your name"
    def authorEmail = ""
    def description = '''\
Brief summary/description of the plugin.
'''

    //watch for all scss file changes
    def watchedResources = ["file:./web-app/**/*.scss", "file:./web-app/**/*.sass"]

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/grails-sass-mine-plugin"

    Boolean loaded = true
    Boolean shouldBeCompiled = System.getProperty("scss.compile")
    ScssDiskCompiler diskCompiler
    ScssResourcesCompiler resourcesCompiler

    def doWithConfigOptions = {
        'mode'(type: String, defaultValue: 'disk')
        'disk.compileOnAnyCommand'(type: Boolean, defaultValue: true)
        //'disk.folders'(type: Map, defaultValue: ['scss': 'scss_css']) - can't define it because of PC bug http://jira.grails.org/browse/GPPLATFORMCORE-44
        'disk.clearTargetFolder'(type: Boolean, defaultValue: true)
        'disk.modules'(type: List, defaultValue: [])
        'disk.checkLastModifiedBeforeCompile'(type: Boolean, defaultValue: true)
        'disk.asyncStartup'(type: Boolean, defaultValue: false)
        'resources.exceptionOnFailedCompilation'(type: Boolean, defaultValue: false)
        'resources.modules.folder.source'(type: String, defaultValue: '')
        'syntax'(type: String, defaultValue: 'byFileDimension')
        'style'(type: String, defaultValue: "compact")
        'lineComments'(type: Boolean, defaultValue: false)
        'debugInfo'(type: Boolean, defaultValue: false)
        'compass'(type: Boolean, defaultValue: false)
        'relativePaths'(type: Boolean, defaultValue: true)
    }

    def onChange = { event ->
        try {
            if(event.source instanceof FileSystemResource) {
                File file = event.source.file

                //similar to https://github.com/bobbywarner/grails-ruby/blob/master/RubyGrailsPlugin.groovy
                if (PluginUtils.isScssFile(file)) {
                    log.info "SCSS: change detected - ${path(file)}"

                    if (PluginUtils.isResourcesMode()) {
                        resourcesCompiler.checkFileAndCompileDependents(file)
                    } else {
                        diskCompiler.checkFileAndCompileWithDependents(file)
                    }
                }
            }
        } catch (Throwable e) {
            println "SCSS: exception on processing change event: ${event} - ${e}"
            e.printStackTrace()
        }
    }

    def onConfigChange = { event ->
        if (PluginUtils.isResourcesMode()) {
            resourcesCompiler?.refreshConfig()
        } else {
            diskCompiler?.refreshConfig()
        }
    }

    def doWithSpring = {
        try {
            if (loaded) {
                ScssConfigHolder.readPluginsConfig(application.config)
                resourcesCompiler = new ScssResourcesCompiler(application)

                if (PluginUtils.isResourcesMode()) {
                    println "SCSS: compiler in resource mode"

                    //refreshing dependencies map
                    resourcesCompiler.calculateDependentFiles(getWatchedFiles(plugin))
                    //enable resources trigger
                    resourcesCompiler.setupResourcesCompileSettings()

                    loaded = false
                }
            }
        } catch (Throwable e) {
            println "SCSS: exception on plugin startup - " + e
            e.printStackTrace()
        }
    }

    def doWithApplicationContext = {
    }

    def doWithWebDescriptor = {
        try {
            if (loaded) {
                ScssConfigHolder.readPluginsConfig(application.config)
                diskCompiler = new ScssDiskCompiler(application)

                if (PluginUtils.isDiskMode()) {
                    println "SCSS: compile in disk mode"

                    //resources mode is disabled... may be we should compile scss
                    if (ScssConfigHolder.config.disk.compileOnAnyCommand || shouldBeCompiled) {
                        if (ScssConfigHolder.config.disk.asyncStartup) {
                            Thread.start {
                                println "SCSS: async startup compile"
                                compileWatchedScssFilesToDisk(plugin)
                            }
                        } else {
                            compileWatchedScssFilesToDisk(plugin)
                        }
                    }

                    loaded = false
                }
            }
        } catch (Throwable e) {
            println "SCSS: exception on plugin startup - " + e
            e.printStackTrace()
        }
    }

    private compileWatchedScssFilesToDisk(plugin) {
        try {
            def files = getWatchedFiles(plugin)
            //refreshing dependencies map
            diskCompiler.calculateDependentFiles(files)
            //may be we should clear target folder?
            if (ScssConfigHolder.config.disk.clearTargetFolder) diskCompiler.clearTargetFolder()
            //let's compile scss files...
            diskCompiler.compileScssFiles(files, ScssConfigHolder.config.disk.checkLastModifiedBeforeCompile)
        } catch (e) {
            println "SCSS: exception on compiling scss files on project startup - " + e
        }
    }

    private List<File> getWatchedFiles(plugin) {
        return plugin.watchedResources.collect { it.file }
    }
}
