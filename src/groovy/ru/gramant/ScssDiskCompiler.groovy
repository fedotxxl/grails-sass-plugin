package ru.gramant

import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.codehaus.groovy.grails.commons.GrailsApplication

@Slf4j
class ScssDiskCompiler {

    private GrailsApplication application
    private ConfigObject config
    private dependentProcessor

    ScssDiskCompiler(GrailsApplication application, ConfigObject config) {
        this.application = application
        this.config = config
        this.dependentProcessor = new ScssDependentProcessor()
    }

    void compileScssFiles(Collection<File> files) {
        log.debug "SCSS: compiling files ${files} from [$sourceFolder] to [$targetFolder]"

        files.each { file ->
            checkFileAndCompile(file)
        }
    }

    void clearTargetFolder() {
        def target = new File('.', targetFolder)

        log.debug "SCSS: cleaning folder [${target.canonicalPath}]"

        if (target.exists()) FileUtils.cleanDirectory(target)
    }

    void checkFileAndCompileWithDependents(File sourceFile) {
        if (checkFileAndCompile(sourceFile)) {
            //compile dependent scss files
            def files = dependentProcessor.getDependentFiles(sourceFile)
            if (files) {
                log.debug "SCSS: compiling dependent on [${sourceFile.name}] files ${files}"

                files.each { file ->
                    checkFileAndCompile(file)
                }
            } else {
                log.debug "SCSS: there is no dependent on [${sourceFile}] files"
            }
        }
    }

    private String getSourceFolder() {
        return FilenameUtils.normalize(config.disk.folder.source)
    }

    private String getTargetFolder() {
        return FilenameUtils.normalize(config.disk.folder.target)
    }

    private File getTargetFile(File sourceFile) {
        return new File(sourceFile.canonicalPath.replace(sourceFolder, targetFolder) + ".css")
    }

    private boolean isTemplate(File file) {
        return file.name.startsWith("_")
    }

    private boolean needToProcess(File file) {
        return file.canonicalPath.contains(sourceFolder)
    }

    private boolean checkFileAndCompile(File sourceFile) {
        if (needToProcess(sourceFile)) {
            if (!isTemplate(sourceFile)) {
                //this is not template... this should be compiled
                def targetFile = getTargetFile(sourceFile)
                def css = ScssUtils.compile(sourceFile, sourceFile.parent, config)

                targetFile.parentFile.mkdirs()
                if (css != null) {
                    targetFile.write(css)
                } else {
                    targetFile.delete()
                }
            }

            log.trace "SCSS: refreshing dependencies for file [${sourceFile}]"
            dependentProcessor.refreshScssFile(sourceFile)

            return true
        } else {
            return false
        }
    }

}
