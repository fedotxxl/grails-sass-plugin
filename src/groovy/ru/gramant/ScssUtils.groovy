/*
 * ScssUtils
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package ru.gramant
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource

import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

class ScssUtils {

    private static final LOG = LoggerFactory.getLogger(this)

    static ScriptEngine jruby = null

    static String compile(GrailsApplication grailsApplication, String template, String path, String syntax, String style, Boolean debugInfo, Boolean lineComments) {
        try {
            LOG.info "Compiling scss template by path ${path}, syntax ${syntax}, style ${style}"

            //:compact, :compressed, :nested,
            //:sass, :scss

            if (!jruby) {
                //process a ruby file
                def rubyFile = new ClassPathResource("myscss.rb").file

                //configure load_path - https://github.com/jruby/jruby/wiki/RedBridge#wiki-Class_Path_Load_Path
                System.setProperty("org.jruby.embed.class.path", rubyFile.parent);
                jruby = new ScriptEngineManager().getEngineByName("jruby");
                jruby.eval(rubyFile.newReader());
            }

            def params = [:]
            params.syntax = (syntax == 'sass') ? 'sass' : 'scss'
            params.style = (style in ['compact', 'compressed', 'nested']) ? syntax : 'compact'
            params.debug_info = (debugInfo) ? true : false
            params.line_comments = (lineComments) ? true : false


            //call a method defined in the ruby source
            jruby.put("template", template);
            jruby.put("params", params);
            jruby.put("loads_path", path)



            return (String) jruby.eval("compileSingleScss(\$template, \$params, \$loads_path)");
        } catch (e) {
            LOG.error("Exception on compiling scss template by path ${path}", e)
            return null
        }
    }

    static String compile(GrailsApplication grailsApplication, String template, String path) {
        def c = new HashMap(grailsApplication.config.grails.sass.flatten())

        return ScssUtils.compile(
                grailsApplication,
                template,
                path,
                c.syntax,
                c.style,
                c.debugInfo as Boolean,
                c.lineComments as Boolean)
    }

}
