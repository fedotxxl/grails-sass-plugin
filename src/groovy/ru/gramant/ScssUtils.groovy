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
@Singleton
class ScssUtils {

    private ScriptEngine jruby = null
    private Boolean compass = false

    String compile(File scssFile, Collection loadPaths, Boolean compass, String syntax, String style, Boolean debugInfo, Boolean lineComments) {
        try {
            def jruby = getJruby(compass)
            def fullLoadPaths = [scssFile.parent] + loadPaths

            log.trace "SCSS: Compiling scss file [${scssFile}], syntax ${syntax}, style ${style}"
            log.trace "SCSS: loadPaths ${fullLoadPaths}"

            def params = [:]
            params.syntax = getSyntax(syntax, scssFile)
            params.style = (style in ['compact', 'compressed', 'nested']) ? syntax : 'compact'
            params.debug_info = (debugInfo) ? true : false
            params.line_comments = (lineComments) ? true : false
            params.scss_folder = scssFile.parent
            params.file_path = FilenameUtils.separatorsToUnix(scssFile.canonicalPath)

            //call a method defined in the ruby source
            jruby.put("template", scssFile.text);
            jruby.put("params", params);
            jruby.put("load_paths", fullLoadPaths)

            def answer = (Map) jruby.eval("compileSingleScss(\$template, \$params, \$load_paths)");
            if (answer.result) {
                return answer.scss
            } else {
                log.warn("SCSS: failed to compile scss file [${scssFile}]: ${answer.short_error}")
                return answer.error
            }
        } catch (RaiseException re) {
            log.error("SCSS: Exception on compiling scss template by path [${scssFile}]")
            return null
        } catch (e) {
            log.error("SCSS: Exception on compiling scss template by path [${scssFile}]", e)
            return null
        }
    }

    String compile(File scssFile, Collection paths, Boolean compass = false, Map config = [:]) {
        return compile(
                scssFile,
                paths,
                compass,
                config.syntax as String,
                config.style as String,
                config.debugInfo as Boolean,
                config.lineComments as Boolean,
        )
    }

    private synchronized getJruby(Boolean compass) {
        if (this.compass != compass) {
            //compass param changed reset jruby
            //current implementation will be very bad when compass param changes frequently
            this.jruby = null
            this.compass = compass
        }

        if (jruby == null) {
            jruby = initJruby(compass)
        }

        return jruby
    }

    private initJruby(Boolean compass) {
        log.debug("SCSS: instantiating jruby instance. Use compass: ${compass}")

        //process a ruby file
        def rubyFileName = compass ? "compassCompiler.rb" : "scssCompiler.rb"
        def rubyFile = new ClassPathResource("ruby/" + rubyFileName).file

        //configure load_path - https://github.com/jruby/jruby/wiki/RedBridge#wiki-Class_Path_Load_Path
        System.setProperty("org.jruby.embed.class.path", rubyFile.parent);
        def jruby = new ScriptEngineManager().getEngineByName("jruby");
        jruby.eval(rubyFile.newReader());

        return jruby
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
        def scssWithoutComments = removeCommentsFromScss(scss)
        scssWithoutComments.findAll(~(/(?m)(?:^|;| )@import([^;\r]*)/), { m, String scssPath ->
            if (scssPath) {
                def paths = extractFileNames(scssPath)
                paths.each { path ->
                    def e = FilenameUtils.getExtension(path)
                    if (path && (!e || !e.equalsIgnoreCase("css")) && !path.startsWith("http://")) {
                        answer << FilenameUtils.getBaseName(path)
                    }
                }
            }
        })

        return answer
    }

    static removeCommentsFromScss(String scss) {
        //http://stackoverflow.com/questions/2613432/remove-source-file-comments-using-intellij
        return scss.replaceAll(/(\/\*([^*]|[\r\n]|(\*+([^*\/]|[\r\n])))*\*+\/|[ \t]*\/\/.*)/, "")
    }

    private static extractFileNames(String atImport) {
        def answer = []
        def current = new StringBuilder()
        def open = null
        def checkComma = false

        for (def ch in atImport) {
            if (!open) {
                if (ch == '"' || ch == "'") {
                    if (checkComma) {
                        return []
                    } else {
                        open = ch
                    }
                } else if (ch == ',') {
                    if (checkComma) {
                        checkComma = false
                        current = new StringBuilder()
                    } else {
                        return []
                    }
                } else if (ch != ' ' && ch != '\t') {
                    return []
                }
            } else if (ch == open) {
                answer << current.toString().trim()
                current = new StringBuilder()
                open = null
                checkComma = true
            } else {
                current.append(ch)
            }
        }

        return answer
    }
}
