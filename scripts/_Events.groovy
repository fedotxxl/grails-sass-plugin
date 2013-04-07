//http://facebook.stackoverflow.com/questions/14942263/how-to-add-source-folder-to-grails-application/14942518#14942518
extraSrcDirs = ["$grailsSassMinePluginPluginDir/src/ruby/"]

eventCompileEnd = {
    copyResources buildSettings.resourcesDir
}

eventCreateWarStart = { warName, stagingDir ->
    if (ru.gramant.ScssCompilerPluginUtils.isResourcesMode()) {
        copyResources "$stagingDir/WEB-INF/classes"
    }
}

private copyResources(destination) {
    ant.copy(todir: destination,
            failonerror: false,
            preservelastmodified: true) {
        for (String path in extraSrcDirs) {
            fileset(dir: path) {
                exclude(name: '*.groovy')
                exclude(name: '*.java')
            }
        }
    }
}