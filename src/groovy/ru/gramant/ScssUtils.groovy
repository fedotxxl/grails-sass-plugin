/*
 * ScssUtils
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package ru.gramant

import javax.script.ScriptEngine
import javax.script.ScriptEngineManager
import org.slf4j.LoggerFactory

class ScssUtils {

    private static final LOG = LoggerFactory.getLogger(this)

    static ScriptEngine jruby = null

    static String compile(String template, String path, String syntax, String style, Boolean debugInfo, Boolean lineComments) {

        LOG.error "Compiling template by path ${path}, syntax ${syntax}, style ${style}"

        //:compact, :compressed, :nested,
        //:sass, :scss

        if (!jruby) {
            jruby = new ScriptEngineManager().getEngineByName("jruby");
            //process a ruby file
            jruby.eval(new BufferedReader(new InputStreamReader(CommonUtils.getClassPathResource("myscss.rb"))));
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
