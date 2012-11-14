import ru.gramant.ScssUtils
import org.apache.commons.io.FilenameUtils

class GrailsSassMinePluginGrailsPlugin {
    // the plugin version
    def version = "0.1.7"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.0 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp"
    ]

    // TODO Fill in these fields
    def title = "Grails Sass Mine Plugin Plugin" // Headline display name of the plugin
    def author = "Your name"
    def authorEmail = ""
    def description = '''\
Brief summary/description of the plugin.
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/grails-sass-mine-plugin"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
//    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
//    def organization = [ name: "My Company", url: "http://www.my-company.com/" ]

    // Any additional developers beyond the author specified above.
//    def developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]

    // Location of the plugin's issue tracker.
//    def issueManagement = [ system: "JIRA", url: "http://jira.grails.org/browse/GPMYPLUGIN" ]

    // Online location of the plugin's browseable source code.
//    def scm = [ url: "http://svn.codehaus.org/grails-plugins/" ]

//    def watchedResources = "file:./**/*.scss"
//    def watchedResources = ['**/*.scss', '**/*.sass']

//    def observe = ['*']

    def doWithWebDescriptor = { xml ->
        println "sass doWithWebDescriptor"
        // TODO Implement additions to web.xml (optional), this event occurs before
    }

    def doWithSpring = {
        // TODO Implement runtime spring config (optional)
    }

    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }

    def doWithApplicationContext = { applicationContext ->
        // TODO Implement post initialization spring config (optional)
    }

    def onChange = { event ->
        println "Changed SCSS file ${event.source.path}. Recompiling"

        /*def source = event.source.file.text
        def compiled = ScssUtils.compile(source)

        def fileName = FilenameUtils.getBaseName(event.source.file.name)
        def file = new File("./web-app/css_scss/${fileName}.css")

        println "Saving recompiled scss ${event.source.file.name} to ${file.absolutePath}"

        file.write(compiled, "UTF-8")*/
    }

    def onConfigChange = { event ->
        println "Sass: config file ${event.source.path} is changed!"

        ConfigObject config = event.source
        def scss = config.get("scss")
    }

    def onShutdown = { event ->
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}
