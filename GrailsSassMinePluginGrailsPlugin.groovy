import org.springframework.core.io.FileSystemResource
import ru.gramant.ScssCompilerPluginUtils
import ru.gramant.ScssConfigHolder
import ru.gramant.ScssDiskCompiler
import ru.gramant.ScssResourcesCompiler

class GrailsSassMinePluginGrailsPlugin {
    // the plugin version
    def version = "0.1.7.20"
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
    Boolean resourcesMode = false
    Boolean shouldBeCompiled = System.getProperty("scss.compile")
    ScssDiskCompiler diskCompiler
    ScssResourcesCompiler resourcesCompiler
    ConfigObject config

    def doWithConfigOptions = {
        'mode'(type: String, defaultValue: 'disk')
        'disk.compileOnAnyCommand'(type: Boolean, defaultValue: true)
        //'disk.folders'(type: Map, defaultValue: ['scss': 'scss_css']) - can't define it because of PC bug http://jira.grails.org/browse/GPPLATFORMCORE-44
        'disk.clearTargetFolder'(type: Boolean, defaultValue: true)
        'disk.modules'(type: List, defaultValue: [])
        'resources.exceptionOnFailedCompilation'(type: Boolean, defaultValue: false)
        'resources.modules.folder.source'(type: String, defaultValue: '')
        'syntax'(type: String, defaultValue: 'byFileDimension')
        'style'(type: String, defaultValue: "compact")
        'lineComments'(type: Boolean, defaultValue: false)
        'debugInfo'(type: Boolean, defaultValue: false)
        'compass'(type: Boolean, defaultValue: false)
    }

    def onChange = { event ->
        try {
            if(event.source instanceof FileSystemResource) {
                File file = event.source.file

                //similar to https://github.com/bobbywarner/grails-ruby/blob/master/RubyGrailsPlugin.groovy
                if (ScssCompilerPluginUtils.isScssFile(file)) {
                    if (resourcesMode) {
                        resourcesCompiler.checkFileAndCompileDependents(file)
                    } else {
                        diskCompiler.checkFileAndCompileWithDependents(file)
                    }
                }
            }
        } catch (Throwable e) {
            println "SCSS: exception on processing change event: ${event}"
            e.printStackTrace()
        }
    }

    def onConfigChange = { event ->
        if (resourcesMode) {
            resourcesCompiler?.refreshConfig()
        } else {
            diskCompiler?.refreshConfig()
        }
    }

    def doWithSpring = {
        try {
            if (loaded) {
                config = ScssConfigHolder.config = ScssCompilerPluginUtils.getPluginsConfig(application.config)
                resourcesMode = ScssCompilerPluginUtils.isResourcesMode(config)

                if (resourcesMode) {
                    println "SCSS: compiler in resource mode"

                    resourcesCompiler = new ScssResourcesCompiler(application)
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
                config = ScssConfigHolder.config = ScssCompilerPluginUtils.getPluginsConfig(application.config)
                resourcesMode = ScssCompilerPluginUtils.isResourcesMode(config)

                if (!resourcesMode) {
                    println "SCSS: compile in disk mode"

                    diskCompiler = new ScssDiskCompiler(application)
                    //resources mode is disabled... may be we should compile scss
                    if (config.disk.compileOnAnyCommand || shouldBeCompiled) {
                        //may be we should clear target folder?
                        if (config.disk.clearTargetFolder) diskCompiler.clearTargetFolder()
                        //let's compile scss files...
                        diskCompiler.compileScssFiles(getWatchedFiles(plugin))
                    }

                    loaded = false
                }
            }
        } catch (Throwable e) {
            println "SCSS: exception on plugin startup - " + e
            e.printStackTrace()
        }
    }

    private List<File> getWatchedFiles(plugin) {
        return plugin.watchedResources.collect { it.file }
    }
}
