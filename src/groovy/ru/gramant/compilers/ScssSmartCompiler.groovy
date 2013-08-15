/*
 * ScssSmartCompiler
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package ru.gramant.compilers

import groovy.util.logging.Slf4j
import org.apache.commons.io.FilenameUtils
import org.jruby.exceptions.RaiseException
import org.jruby.javasupport.JavaEmbedUtils
import org.springframework.core.io.ClassPathResource

import java.util.concurrent.CopyOnWriteArrayList

@Slf4j
@Singleton
class ScssSmartCompiler {

    private ScssCompiler compiler

    void reset(Boolean compass, Integer threadsCount) {

    }

    String compile(File scssFile, Collection loadPaths, String syntax, String style, boolean debugInfo, boolean lineComments, boolean sourcemap) {
        try {
            def jrubyAnswer
            jruby = jrubies.take()
            def fullLoadPaths = [scssFile.parent] + loadPaths

            log.trace "SCSS: Compiling scss file [${scssFile}], syntax ${syntax}, style ${style}"
            log.trace "SCSS: loadPaths ${fullLoadPaths}"

            def params = [:]
            params.syntax = getSyntax(syntax, scssFile)
            params.style = (style in ['compact', 'compressed', 'nested', 'expanded']) ? style : 'nested'
            params.debug_info = debugInfo
            params.line_comments = lineComments
            params.scss_folder = scssFile.parent
            params.file_path = FilenameUtils.separatorsToUnix(scssFile.canonicalPath)
            params.sourcemap = sourcemap
            params.compass_root = compassRoot

            //call a method defined in the ruby source
            def c = jruby.container
            def u = jruby.unit
            c.put("@template", scssFile.text);
            c.put("@params", params);
            c.put("@load_paths", fullLoadPaths as CopyOnWriteArrayList)

            jrubyAnswer = (Map) JavaEmbedUtils.rubyToJava(u.run())

            if (jrubyAnswer.result) {
                return jrubyAnswer.scss
            } else {
                log.warn("SCSS: failed to compile scss file [${scssFile}]: ${jrubyAnswer.short_error}")
                return jrubyAnswer.error
            }
        } catch (RaiseException re) {
            log.error("SCSS: Exception on compiling scss template by path [${scssFile}]")
            return null
        } catch (e) {
            log.error("SCSS: Exception on compiling scss template by path [${scssFile}]", e)
            return null
        } finally {
            if (jruby) jrubies.put(jruby)
        }
    }

    String compile(File scssFile, Collection paths, Boolean compass = false, Map config = [:]) {
        return compile(
                scssFile,
                paths,
                compass,
                config.syntax as String,
                config.style as String,
                config.debugInfo as boolean,
                config.lineComments as boolean,
                config.sourcemap as boolean
        )
    }

    private getCompassRoot() {
        //unable to extract compass sources into separate jar
        //http://stackoverflow.com/questions/15549617/cleanest-way-to-run-susy-compass-and-sass-within-jruby-complete
        //http://henningpetersen.com/post/7/integrating-compass-style-sass-into-tapestry
        def path = new ClassPathResource("ruby/compassCompiler.rb").URL.path
        def file = new File(path)

        return file.parent
    }

    private getSyntax(String option, File scssFile) {
        if (option == 'byFileDimension') {
            option = FilenameUtils.getExtension(scssFile.name)
        }

        if (option == 'sass') {
            return 'sass'
        } else {
            return 'scss'
        }
    }
}
