import ru.gramant.ScssCompiler

class GrailsSassMinePluginGrailsPlugin {
    // the plugin version
    def version = "0.1.7.11"
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
    def watchedResources = "file:./web-app/**/*.scss"

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/grails-sass-mine-plugin"

    Boolean firstTime = true
    Boolean shouldBeCompiled = System.getProperty("scss.compile")
    ScssCompiler compiler

    def doWithConfigOptions = {
        'compileOnAnyCommand'(type: Boolean, defaultValue: false)
        'resourcesMode'(type: Boolean, defaultValue: false)
        'folder.source'(type: String, defaultValue: '/web-app/scss')
        'folder.target'(type: String, defaultValue: '/web-app/scss_css')
        'clearTargetFolder'(type: Boolean, defaultValue: true)
        'syntax'(type: String, defaultValue: 'byFileDimension')
        'style'(type: String, defaultValue: "compact")
        'lineComments'(type: Boolean, defaultValue: false)
        'debug'(type: Boolean, defaultValue: false)
    }

    def onChange = { event ->
        if (!compiler.config.resourcesMode) {
            compiler.checkFileAndCompileWithDependents(event.source.file)
        }
    }

    def doWithSpring = {
    }

    def doWithApplicationContext = {
    }

    def doWithWebDescriptor = {
        if (firstTime) {
            compiler = new ScssCompiler(application)

            if (!compiler.config.resourcesMode) {
                //resources mode is disabled... may be we should compile scss
                if (compiler.config.compileOnAnyCommand || shouldBeCompiled) {
                    //may be we should clear target folder?
                    if (compiler.config.clearTargetFolder) compiler.clearTargetFolder()
                    //let's compile scss files...
                    compiler.compileScssFiles(plugin.watchedResources.collect { it.file })
                }
            }
        }

        firstTime = false
    }
}
