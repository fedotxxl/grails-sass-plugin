package ru.gramant
import grails.util.PluginBuildSettings
import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.plugins.GrailsPluginUtils

@Slf4j
class ScssDiskCompiler extends AbstractScssCompiler {

//    private GrailsApplication application
//    private ConfigObject config
    private ScssDependentProcessor dependentProcessor
    private PluginBuildSettings pluginBuildSettings
    private String projectSourcePath
    private String sourceFolder
    private String targetFolder

    ScssDiskCompiler(GrailsApplication application) {
        super(application)
//        this.application = application
//        this.config = config
        this.dependentProcessor = new ScssDependentProcessor()
        this.pluginBuildSettings = GrailsPluginUtils.getPluginBuildSettings()

        refreshConfig()
    }

    void refreshConfig() {
        super.refreshConfig();
        this.sourceFolder = calculateSourceFolder()
        this.targetFolder = calculateTargetFolder()
        this.projectSourcePath = new File('.', sourceFolder).canonicalPath
    }

    void compileScssFiles(Collection<File> files) {
        def filteredFiles = files.findAll { needToProcess(it) }

        log.debug "SCSS: compiling files ${filteredFiles} from [$sourceFolder] to [$targetFolder]"

        filteredFiles.each { file ->
            compileScssFile(file)
        }
    }

    void clearTargetFolder() {
        def target = new File('.', targetFolder)

        log.debug "SCSS: cleaning folder [${target.canonicalPath}]"

        if (target.exists()) FileUtils.cleanDirectory(target)
    }

    void checkFileAndCompileWithDependents(File sourceFile) {
        if (needToProcess(sourceFile)) {
            log.debug "Checking file [${sourceFile}] and compile dependent on it files"

            //compile changed file
            compileScssFile(sourceFile)
            //compile dependent scss files
            def files = dependentProcessor.getDependentFiles(sourceFile)
            if (files) {
                log.debug "SCSS: compiling dependent on [${sourceFile.name}] files ${files}"

                files.each { file ->
                    compileScssFile(file)
                }
            } else {
                log.debug "SCSS: there is no dependent on [${sourceFile}] files"
            }
        }
    }

    private File getTargetFile(File sourceFile) {
        return new File(sourceFile.canonicalPath.replace(sourceFolder, targetFolder) + ".css")
    }

    private boolean isTemplate(File file) {
        return file.name.startsWith("_")
    }

    private boolean needToProcess(File file) {
        def path = file.canonicalPath
        return ScssCompilerPluginUtils.pathContains(path, projectSourcePath) || scssCompilePaths.any { ScssCompilerPluginUtils.pathContains(path, it) }
    }

    private compileScssFile(File sourceFile) {
        if (!isTemplate(sourceFile)) {
            //this is not template... this should be compiled
            def targetFile = getTargetFile(sourceFile)
            def css = ScssUtils.compile(sourceFile, scssCompilePaths, config.compass, config)

            targetFile.parentFile.mkdirs()
            if (css != null) {
                targetFile.write(css)
            } else {
                targetFile.delete()
            }
        }

        log.trace "SCSS: refreshing dependencies for file [${sourceFile}]"
        dependentProcessor.refreshScssFile(sourceFile)
    }

//    private calculateModulesScssPaths() {
//        def answer = []
//        def modules = config.disk.modules
//
//        if (modules) {
//            modules.each { module ->
//                def moduleFolder = pluginBuildSettings.getPluginDirForName(module)?.file
//                if (moduleFolder) {
//                    def file = new File(moduleFolder, sourceFolder)
//                    if (file.exists()) answer << file.canonicalPath
//                }
//            }
//        }
//
//        return answer
//    }

    private String calculateSourceFolder() {
        return FilenameUtils.normalize('/web-app/' + config.disk.folder.source)
    }

    private String calculateTargetFolder() {
        return FilenameUtils.normalize('/web-app/' + config.disk.folder.target)
    }
}
