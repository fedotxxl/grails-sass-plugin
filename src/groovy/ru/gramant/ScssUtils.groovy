/*
 * ScssUtils
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package ru.gramant
import groovy.util.logging.Slf4j
import org.apache.commons.io.FilenameUtils
import org.jruby.exceptions.RaiseException
import org.springframework.core.io.ClassPathResource

import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

@Slf4j
class ScssUtils {

    static ScriptEngine jruby = null

    static String compile(File scssFile, String path, String syntax, String style, Boolean debugInfo, Boolean lineComments) {
        try {
            log.info "SCSS: Compiling scss file [${scssFile}], syntax ${syntax}, style ${style}"

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
            params.syntax = getSyntax(syntax, scssFile)
            params.style = (style in ['compact', 'compressed', 'nested']) ? syntax : 'compact'
            params.debug_info = (debugInfo) ? true : false
            params.line_comments = (lineComments) ? true : false

            //call a method defined in the ruby source
            jruby.put("template", scssFile.text);
            jruby.put("params", params);
            jruby.put("loads_path", path)

            return (String) jruby.eval("compileSingleScss(\$template, \$params, \$loads_path)");
        } catch (RaiseException re) {
            log.error("SCSS: Exception on compiling scss template by path [${path}]")
            return null
        } catch (e) {
            log.error("SCSS: Exception on compiling scss template by path [${path}]", e)
            return null
        }
    }

    static String compile(File scssFile, String path, Map config = [:]) {
        return ScssUtils.compile(
                scssFile,
                path,
                config.syntax as String,
                config.style as String,
                config.debugInfo as Boolean,
                config.lineComments as Boolean)
    }

    static String getSyntax(String option, File scssFile) {
        if (option == 'byFileDimension') {
            option = FilenameUtils.getExtension(scssFile.name)
        }

        if (option == 'sass') {
            return 'sass'
        } else {
            return 'scss'
        }
    }

    static String getScssName(String path) {
        def fileName = FilenameUtils.getBaseName(path)

        //remove template prefix and return
        return (fileName.startsWith("_")) ? fileName.substring(1) : fileName
    }

    static Set<String> getDependOnScssNames(String scss) {
        def answer = [] as Set
        scss.findAll(~(/@import (?: *)"([a-zA-Z0-9\/\\ _]+)"(?: *);/), { m, String scssPath ->
            def from = -1
            from = Math.max(from, scssPath.lastIndexOf("\\"))
            from = Math.max(from, scssPath.lastIndexOf("/"))

            answer << scssPath.substring(from+1)
        })

        return answer
    }
}
