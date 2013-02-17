package ru.gramant

import org.apache.commons.io.FileUtils

class PluginSettings {
    static BuildCommand buildCommand = null
    static boolean useResourcesPlugin = false
    static ScssDependentProcessor dependentProcessor = new ScssDependentProcessor()
    static grailsApplication

    static File getSourceFolder() {
        return new File('.', '.\\web-app/scss/')
    }

    static File getTargetFolder() {
        return new File('.', './web-app/scss_css/')
    }

    static File getTargetFile(File sourceFile) {
        return new File(targetFolder, getCompileRelativePath(sourceFile))
    }

    static boolean needToCompile(File file) {
        return FileUtils.directoryContains(sourceFolder, file) && !file.name.startsWith("_")
    }

    static String getCompileRelativePath(File file) {
        return ResourceUtils.getRelativePath(file.canonicalPath, sourceFolder.canonicalPath, File.separator)
    }

    static boolean checkFileAndCompile(File sourceFile) {
        if (PluginSettings.needToCompile(sourceFile)) {
            def targetFile = getTargetFile(sourceFile)
            def scss = sourceFile.text
            def css = ScssUtils.compile(grailsApplication, scss, sourceFile.parent)

            targetFile.parentFile.mkdirs()
            if (css) {
                targetFile.write(css)
            } else {
                targetFile.write(scss)
            }

            //refresh dependent scss files
            dependentProcessor.refreshScssFile(sourceFile)

            return true
        } else {
            return false
        }
    }

    static void checkFileAndCompileWithDependents(File sourceFile) {
        if (checkFileAndCompile(sourceFile)) {
            //compile dependent scss files
            dependentProcessor.getDependentFiles(sourceFile).each { file ->
                checkFileAndCompile(file)
            }
        }
    }
}