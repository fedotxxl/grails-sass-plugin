import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.io.FileSystemResource
import ru.gramant.ScssCompilePathProcessor
import ru.gramant.ScssCompilerPluginUtils as PluginUtils
import ru.gramant.ScssConfigHolder
import ru.gramant.ScssDiskCompiler
import ru.gramant.ScssResourcesCompiler

import static ru.gramant.ScssCompilerPluginUtils.path

class GrailsSassMinePluginGrailsPlugin {

    private static final Logger LOG = LoggerFactory.getLogger("ru.grails.GrailsSassMinePluginGrailsPlugin")

    // the plugin version
    def version = "0.1.7.23"
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
        'disk.checkLastModifiedBeforeCompile'(type: Boolean, defaultValue: false)
        'disk.asyncStartup'(type: Boolean, defaultValue: false)
        'resources.exceptionOnFailedCompilation'(type: Boolean, defaultValue: false)
        'resources.modules.folder.source'(type: String, defaultValue: '')
        'syntax'(type: String, defaultValue: 'byFileDimension')
        'style'(type: String, defaultValue: "compact")
        'lineComments'(type: Boolean, defaultValue: false)
        'debugInfo'(type: Boolean, defaultValue: false)
        'compass'(type: Boolean, defaultValue: false)
        'relativePaths'(type: Boolean, defaultValue: true)
        'compilePathExclude'(type: List, defaultValue: [])
    }

    def onChange = { event ->
        try {
            if(event.source instanceof FileSystemResource) {
                File file = event.source.file

                //similar to https://github.com/bobbywarner/grails-ruby/blob/master/RubyGrailsPlugin.groovy
                if (PluginUtils.isScssFile(file)) {
                    LOG.info "SCSS: change detected - ${path(file)}"

                    //update compile path
                    ScssCompilePathProcessor.instance.addFolderToCompilePath(file.parentFile)

                    //recompile scss file
                    if (PluginUtils.isResourcesMode()) {
                        resourcesCompiler.checkFileAndCompileDependents(file)
                    } else {
                        diskCompiler.checkFileAndCompileWithDependents(file)
                    }
                }
            }
        } catch (Throwable e) {
            LOG.error("SCSS: exception on processing change event: ${event} - ${e}", e)
        }
    }

    def onConfigChange = { event ->
        //update compile path
        refreshScssCompilePaths(plugin)

        //update compilers
        if (PluginUtils.isResourcesMode()) {
            resourcesCompiler?.refreshConfig()
        } else {
            diskCompiler?.refreshConfig()
        }
    }

    def doWithSpring = {
        try {
            if (loaded) {
                initConfigHolderAndCompilePathProcessor(application, plugin)
                resourcesCompiler = new ScssResourcesCompiler(application)

                if (PluginUtils.isResourcesMode()) {
                    LOG.info "SCSS: compiler in resource mode"

                    //refreshing dependencies map
                    resourcesCompiler.calculateDependentFiles(getWatchedFiles(plugin))
                    //enable resources trigger
                    resourcesCompiler.setupResourcesCompileSettings()

                    loaded = false
                }
            }
        } catch (Throwable e) {
            LOG.error("SCSS: exception on plugin startup - " + e, e)
        }
    }

    def doWithApplicationContext = {
    }

    def doWithWebDescriptor = {
        try {
            if (loaded) {
                initConfigHolderAndCompilePathProcessor(application, plugin)
                diskCompiler = new ScssDiskCompiler(application)

                if (PluginUtils.isDiskMode()) {
                    LOG.info "SCSS: compile in disk mode"

                    //resources mode is disabled... may be we should compile scss
                    if (ScssConfigHolder.config.disk.compileOnAnyCommand || shouldBeCompiled) {
                        if (ScssConfigHolder.config.disk.asyncStartup) {
                            Thread.start {
                                LOG.info "SCSS: async startup compile"
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
            LOG.error("SCSS: exception on plugin startup - " + e, e)
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
            LOG.error("SCSS: exception on compiling scss files on project startup - " + e, e)
        }
    }

    private List<File> getWatchedFiles(plugin) {
        return plugin.watchedResources.collect { it.file }
    }

    private initConfigHolderAndCompilePathProcessor(application, plugin) {
        ScssConfigHolder.readPluginsConfig(application.config)
        ScssCompilePathProcessor.instance.recalculateCompilePath(getWatchedFiles(plugin))
    }

    private refreshScssCompilePaths(plugin) {
        ScssCompilePathProcessor.instance.refreshConfig()
        ScssCompilePathProcessor.instance.recalculateCompilePath(getWatchedFiles(plugin))
    }

}
