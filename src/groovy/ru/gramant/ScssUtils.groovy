/*
 * ScssUtils
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package ru.gramant
import groovy.util.logging.Slf4j
import org.apache.commons.io.FilenameUtils
import org.jruby.embed.LocalContextScope
import org.jruby.embed.ScriptingContainer
import org.springframework.core.io.ClassPathResource

import javax.script.ScriptEngine
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue

@Slf4j
@Singleton
class ScssUtils {

    private BlockingQueue<Map> jrubies = new ArrayBlockingQueue(4)
    private ScriptEngine jruby = null
    private Boolean compass = false

    ScssUtils() {
        4.times {
            jrubies.put(initJruby(true));
        }
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

    private synchronized initJruby(Boolean compass) {
        log.debug("SCSS: instantiating jruby instance. Use compass: ${compass}")

        //process a ruby file
        def rubyFileName = compass ? "compassCompiler.rb" : "scssCompiler.rb"

        //configure load_path - https://github.com/jruby/jruby/wiki/RedBridge#wiki-Class_Path_Load_Path
        System.setProperty("org.jruby.embed.class.path", "classpath:ruby");
        def container = new ScriptingContainer(LocalContextScope.SINGLETHREAD)
        def unit = container.parse(new InputStreamReader(new ClassPathResource("ruby/${rubyFileName}").inputStream).text)
        return [container: container, unit: unit]
    }

    /**
     * @return module name extracted from {@code path}
     * "a/b -> b"   ]
     * "a/_b -> b"
     * "a/b.scss -> b"
     * "a -> a"
     */
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
                    def e = FilenameUtils.getExtension(path).toLowerCase()
                    if (path && (e != "css") && !path.startsWith("http://")) {
//                        if (e == "scss" || e == "sass") {
//                            answer << FilenameUtils.getBaseName(path)
//                        } else {
//                            answer << path
//                        }

                        answer << getScssName(path)
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
