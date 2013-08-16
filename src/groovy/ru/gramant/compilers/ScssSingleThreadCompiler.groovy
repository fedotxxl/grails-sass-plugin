/*
 * ScssSingleThreadCompiler
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package ru.gramant.compilers

import groovy.util.logging.Slf4j
import org.springframework.core.io.ClassPathResource

import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

@Slf4j
@Singleton
class ScssSingleThreadCompiler implements ScssCompiler {

    private ScriptEngine jruby = null
    private Boolean compass = null

    @Override
    void setCompass(Boolean compass) {
        this.compass = compass
        resetJruby()
    }

    synchronized Map compile(File scssFile, Map params, Collection fullLoadPaths) {
        //call a method defined in the ruby source
        jruby.put("template", scssFile.text);
        jruby.put("params", params);
        jruby.put("load_paths", fullLoadPaths)

        return (Map) jruby.eval("compileSingleScss(\$template, \$params, \$load_paths)");
    }

    private resetJruby() {
        jruby = initJruby(compass)
    }

    private synchronized initJruby(Boolean compass) {
        log.debug("SCSS: instantiating jruby instance. Use compass: ${compass}")

        //process a ruby file
        def rubyFileName = compass ? "compassCompiler_2.rb" : "scssCompiler.rb"

        //configure load_path - https://github.com/jruby/jruby/wiki/RedBridge#wiki-Class_Path_Load_Path
        System.setProperty("org.jruby.embed.class.path", "classpath:ruby");
        def jruby = new ScriptEngineManager().getEngineByName("jruby");
        jruby.eval(new InputStreamReader(new ClassPathResource("ruby/${rubyFileName}").inputStream));

        return jruby
    }
}
