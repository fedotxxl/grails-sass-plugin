import org.grails.plugin.resource.BundleResourceMapper
import org.grails.plugin.resource.CSSBundleResourceMeta
import org.grails.plugin.resource.CSSPreprocessorResourceMapper
import org.grails.plugin.resource.CSSRewriterResourceMapper
import org.grails.plugin.resource.ResourceModule
import org.grails.plugin.resource.ResourceProcessor
import org.grails.plugin.resource.ResourceTagLib

class GrailsSassMinePluginGrailsPlugin {
    // the plugin version
    def version = "0.1.7.8"
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

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/grails-sass-mine-plugin"

    def doWithSpring = {
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

}
