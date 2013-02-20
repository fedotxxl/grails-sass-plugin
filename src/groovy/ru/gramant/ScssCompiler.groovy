package ru.gramant

import org.apache.commons.io.FileUtils
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.grails.plugin.resource.ResourceProcessor
import org.grails.plugin.resource.ResourceTagLib


class ScssCompiler {

    private GrailsApplication application
    private config
    private dependentProcessor

    ScssCompiler(GrailsApplication application) {
        this.application = application
        this.config = application.config.plugin.grailsSassMinePlugin
        this.dependentProcessor = new ScssDependentProcessor()
    }

    def getConfig() {
        return config
    }

    void compileScssFiles(Collection<File> files) {
        files.each { file ->
            checkFileAndCompile(file)
        }
    }

    void clearTargetFolder() {

    }

    void checkFileAndCompileWithDependents(File sourceFile) {
        if (checkFileAndCompile(sourceFile)) {
            //compile dependent scss files
            dependentProcessor.getDependentFiles(sourceFile).each { file ->
                checkFileAndCompile(file)
            }
        }
    }

    void doResourcesModePreparation() {
        CSSPreprocessorResourceMapper.defaultIncludes.add('**/*.scss')
        CSSRewriterResourceMapper.defaultIncludes.add('**/*.scss')

//        BundleResourceMapper.MIMETYPE_TO_RESOURCE_META_CLASS.put('stylesheet', CSSBundleResourceMeta)
//        List currentTypes = new ResourceModule().bundleTypes
//        ResourceModule.metaClass.getBundleTypes = {  currentTypes << 'scss' }
        ResourceProcessor.DEFAULT_MODULE_SETTINGS['scss'] = [disposition: 'head'  ]
        ResourceTagLib.SUPPORTED_TYPES['scss'] = [
                type: "text/css",
                rel: 'stylesheet',
                media: 'screen, projection'
        ]
    }

    private File getSourceFolder() {
        return new File('.', config.folder.source)
    }

    private File getTargetFolder() {
        return new File('.', config.folder.target)
    }

    private File getTargetFile(File sourceFile) {
        return new File(targetFolder, getCompileRelativePath(sourceFile) + ".css")
    }

    private boolean isTemplate(File file) {
        return file.name.startsWith("_")
    }

    private boolean needToProcess(File file) {
        return FileUtils.directoryContains(sourceFolder, file)
    }

    private String getCompileRelativePath(File file) {
        return ResourceUtils.getRelativePath(file.canonicalPath, sourceFolder.canonicalPath, File.separator)
    }

    private boolean checkFileAndCompile(File sourceFile) {
        if (needToProcess(sourceFile)) {
            if (!isTemplate(sourceFile)) {
                //this is not template... this should be compiled
                def targetFile = getTargetFile(sourceFile)
                def scss = sourceFile.text
                def css = ScssUtils.compile(application, scss, sourceFile.parent)

                targetFile.parentFile.mkdirs()
                if (css) {
                    targetFile.write(css)
                } else {
                    targetFile.write(scss)
                }
            }

            //refresh dependent scss files
            dependentProcessor.refreshScssFile(sourceFile)

            return true
        } else {
            return false
        }
    }

}
