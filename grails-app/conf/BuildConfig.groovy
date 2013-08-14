grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"

grails.project.target.level = 1.6
grails.project.source.level = 1.6

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {
        mavenLocal()
        grailsCentral()
        mavenCentral()
    }

    dependencies {
        compile "bsf:bsf:2.4.0"
        compile "io.belov.grails:sass-compass:0.12.2.2.11"
        runtime "org.jruby:jruby-complete:1.6.8"
        compile "commons-io:commons-io:2.4"
        test "org.spockframework:spock-grails-support:0.7-groovy-2.0"
    }

    plugins {
        build(":tomcat:$grailsVersion",
              ":release:2.0.3",
              ":rest-client-builder:1.0.2",
        ) {
            export = false
        }

        test(":spock:0.7") {
            export = false
            exclude "spock-grails-support"
        }

//        compile ":resources:1.1.6"
        compile ":platform-core:1.0.RC5"
    }
}