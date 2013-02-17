/*
 * ScssUtils
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package ru.gramant

import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.core.io.ClassPathResource

import javax.script.ScriptEngine
import javax.script.ScriptEngineManager
import org.slf4j.LoggerFactory

class ScssUtils {

    private static final LOG = LoggerFactory.getLogger(this)

    static ScriptEngine jruby = null

    static String compile(GrailsApplication grailsApplication, String template, String path, String syntax, String style, Boolean debugInfo, Boolean lineComments) {

        LOG.info "Compiling template by path ${path}, syntax ${syntax}, style ${style}"

        //:compact, :compressed, :nested,
        //:sass, :scss

        if (!jruby) {
            jruby = new ScriptEngineManager().getEngineByName("jruby");
            //process a ruby file
            def rubyFile = new ClassPathResource("myscss.rb")
            jruby.eval(new BufferedReader(new InputStreamReader(rubyFile.inputStream)));
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
    }

}
