package ru.gramant
import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.filefilter.IOFileFilter
import org.codehaus.groovy.grails.plugins.GrailsPluginUtils

import static ru.gramant.ScssCompilerPluginUtils.path
import static ru.gramant.ScssCompilerPluginUtils.paths

@Slf4j
class ScssDiskCompiler {

    private ScssDependentProcessor dependentProcessor
    private Map<String, List<String>> folders

    ScssDiskCompiler() {
        this.dependentProcessor = new ScssDependentProcessor()
        refreshConfig()
    }

    void refreshConfig() {
        this.folders = calculateFolders()
    }

    void calculateDependentFiles(Collection<File> files) {
        log.debug "SCSS: refreshing dependencies for files ${paths(files)}"

        files.each {
            dependentProcessor.refreshScssFile(it)
        }
    }

    void compileScssFiles(Collection<File> files, Boolean checkLastModifiedBeforeCompile = false) {
        def filteredFiles = files.findAll { needToProcess(it) }

        filteredFiles.each { file ->
            compileScssFileAsync(file, checkLastModifiedBeforeCompile)
        }
    }

    void clearTargetFolder() {
        def targetFolders = folders.values().flatten() as Set
        def parentFolders = GrailsPluginUtils.pluginBuildSettings.getInlinePluginDirectories().collect {it.file} + projectFolder

        targetFolders.each { folder ->
            parentFolders.each { parentFolder ->
                def file = new File(folder, parentFolder)
                if (file.exists() && file.isDirectory()) doClearTargetFolder(file)
            }
        }
    }

    private void doClearTargetFolder(File file) {
        //select files to delete
        def files = FileUtils.listFiles(file, new IOFileFilter() {
            @Override
            boolean accept(File f) {
                return isScssCompiledFile(f)
            }

            @Override
            boolean accept(File dir, String name) {
                return isScssCompiledFile(new File(name, dir))
            }
        }, null)

        log.info("SCSS: clearing target folder: ${path(file)} - ${files.size()} files to delete")

        //delete selected files
        files.each { fileToDelete ->
            log.trace("SCSS: deleting file ${path(fileToDelete)}")
            fileToDelete.delete()
        }
    }

    void checkFileAndCompileWithDependents(File sourceFile, Boolean checkLastModifiedBeforeCompile = false) {
        if (needToProcess(sourceFile)) {
            log.debug "SCSS: Checking file [${path(sourceFile)}] and compile dependent on it files"

            //compile changed file
            compileScssFileAsync(sourceFile, checkLastModifiedBeforeCompile)
            //compile dependent scss files
            def files = dependentProcessor.getDependentFiles(sourceFile)
            if (files) {
                log.debug "SCSS: compiling dependent on [${path(sourceFile)}] files ${paths(files)}"

                files.each { file ->
                    compileScssFileAsync(file, checkLastModifiedBeforeCompile)
                }
            } else {
                log.debug "SCSS: there is no dependent on [${path(sourceFile)}] files"
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
        if (ScssPluginUtils.isProjectOrInlinePluginFile(file)) {
            return getTargetFoldersForFile(file) as boolean
        } else {
            log.trace("SCSS: file ${path(file)} will not be processed")
            return false //don't compile external files
        }
    }

    private compileScssFileAsync(File sourceFile, Boolean checkLastModifiedBeforeCompile = false) {
        Thread.start {
            compileScssFile(sourceFile, checkLastModifiedBeforeCompile)
        }
    }

    private compileScssFile(File sourceFile, Boolean checkLastModifiedBeforeCompile = false) {
        log.trace "SCSS: refreshing dependencies for file [${path(sourceFile)}]"
        dependentProcessor.refreshScssFile(sourceFile)

        if (!isTemplate(sourceFile)) {
            //this is not template... this should be compiled
            def targetFiles = getTargetFiles(sourceFile)

            if (!checkLastModifiedBeforeCompile || isModifiedSinceLastCompile(sourceFile, targetFiles)) {
                log.debug "SCSS: compiling file ${path(sourceFile)} to ${paths(targetFiles)}"

                def css = ScssUtils.instance.compile(sourceFile, ScssCompilePathProcessor.instance.compilePath, ScssConfigHolder.config.compass, ScssConfigHolder.config)

                targetFiles.each { targetFile ->
                    targetFile.parentFile.mkdirs()
                    if (css != null) {
                        log.info "SCSS: overwrite ${path(targetFile)}"

                        targetFile.write(css)
                    } else {
                        targetFile.delete()
                    }
                }
            } else {
                log.info "SCSS: skip compiling file ${path(sourceFile)} to ${paths(targetFiles)}: up to date"
            }
        }
    }

    /**
     * @return true if {@code targetFiles} was modified earlier than {@code sourceFile} and its dependencies
     */
    private isModifiedSinceLastCompile(File sourceFile, List<File> targetFiles) {
        def minModifiedDate = (dependentProcessor.getDependsOnFiles(sourceFile) + sourceFile)
                .max {a, b -> a.lastModified() <=> b.lastModified()}
                .lastModified()

        for (def targetFile in targetFiles) {
            if (!targetFile.exists() || minModifiedDate > targetFile.lastModified()) return true
        }

        return false
    }

    private getTargetFoldersForFile(File file) {
        def key = getSourceFolderKeyForFile(file)
        return (key) ? folders[key] : null
    }

    private getSourceFolderKeyForFile(File file) {
        return folders.keySet().find { ScssCompilerPluginUtils.pathContains(file.canonicalPath, it) }
    }

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
        def defaultValue = ['scss': 'css_scss']
        def configValue = ScssConfigHolder.config.disk.folders

        return (configValue instanceof Map) ? configValue : defaultValue
    }

    private isScssCompiledFile(File file) {
        def fileName = file.name.toLowerCase()
        return fileName.endsWith(".scss.css") || fileName.endsWith(".sass.css")
    }
}
