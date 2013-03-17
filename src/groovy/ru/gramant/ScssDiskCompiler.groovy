package ru.gramant

import groovy.util.logging.Slf4j
import org.apache.commons.io.FilenameUtils
import org.codehaus.groovy.grails.commons.GrailsApplication

@Slf4j
class ScssDiskCompiler extends AbstractScssCompiler {

//    private GrailsApplication application
//    private ConfigObject config
    private ScssDependentProcessor dependentProcessor
    private Map folders

    ScssDiskCompiler(GrailsApplication application) {
        super(application)
//        this.application = application
//        this.config = config
        this.dependentProcessor = new ScssDependentProcessor()

        refreshConfig()
    }

    void refreshConfig() {
        super.refreshConfig();
        this.folders = calculateFolders()
    }

    void compileScssFiles(Collection<File> files) {
        def filteredFiles = files.findAll { needToProcess(it) }

        filteredFiles.each { file ->
            compileScssFile(file)
        }
    }

    void clearTargetFolder() {
//        def target = new File('.', targetFolder)
//
//        log.debug "SCSS: cleaning folder [${target.canonicalPath}]"
//
//        if (target.exists()) FileUtils.cleanDirectory(target)
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

    private List<File> getTargetFiles(File sourceFile) {
        def sourcePath = sourceFile.canonicalPath
        def sourceFolderKey = getSourceFolderKeyForFile(sourceFile)
        def targetFolders = folders[sourceFolderKey]

        return targetFolders.collect { targetFolder ->
            new File(sourcePath.replace(sourceFolderKey, targetFolder) + ".css")
        }
    }

    private boolean isTemplate(File file) {
        return file.name.startsWith("_")
    }

    private boolean needToProcess(File file) {
        if (isProjectOrInlinePluginFile(file)) {
            return getTargetFoldersForFile(file) as boolean
        } else {
            log.trace("SCSS: file ${file} will not be processed")
            return false //don't compile external files
        }
    }

    private compileScssFile(File sourceFile) {
        if (!isTemplate(sourceFile)) {
            //this is not template... this should be compiled
            def targetFiles = getTargetFiles(sourceFile)

            log.debug "SCSS: compiling file ${sourceFile} to ${targetFiles}"

            def css = ScssUtils.compile(sourceFile, scssCompilePaths, config.compass, config)

            targetFiles.each { targetFile ->
                targetFile.parentFile.mkdirs()
                if (css != null) {
                    targetFile.write(css)
                } else {
                    targetFile.delete()
                }
            }
        }

        log.trace "SCSS: refreshing dependencies for file [${sourceFile}]"
        dependentProcessor.refreshScssFile(sourceFile)
    }

    private getTargetFoldersForFile(File file) {
        def key = getSourceFolderKeyForFile(file)
        return (key) ? folders[key] : null
    }

    private getSourceFolderKeyForFile(File file) {
        return folders.keySet().find { ScssCompilerPluginUtils.pathContains(file.canonicalPath, it) }
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

    private Map calculateFolders() {
        def answer = [:].withDefault { [] }

        diskFolders.each {
            def from = it.key
            def to = ScssCompilerPluginUtils.toList(it.value)

            if (from && to) {
                answer[getWebAppSubFolder(from)] = to.collect { getWebAppSubFolder(it) }
            }
        }

        return answer
    }

    private getWebAppSubFolder(String path) {
        return FilenameUtils.normalize('/web-app/' + path)
    }

    private getDiskFolders() {
        def defaultValue = ['scss': 'scss_css']
        def configValue = config.disk.folders

        return (configValue instanceof Map) ? configValue : defaultValue
    }
}
