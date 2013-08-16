/*
 * ScssMultiThreadCompiler
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package ru.gramant.compilers
import groovy.util.logging.Slf4j
import org.jruby.embed.LocalContextScope
import org.jruby.embed.ScriptingContainer
import org.jruby.javasupport.JavaEmbedUtils
import org.springframework.core.io.ClassPathResource

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.CopyOnWriteArrayList

@Slf4j
@Singleton
class ScssMultiThreadCompiler implements ScssCompiler {

    private BlockingQueue<Map> jrubies
    private Boolean compass = null
    private Integer threadsCount = -1

    void setThreadsCount(Integer count) {
        threadsCount = count
        jrubies = new ArrayBlockingQueue<>(count)
        resetQueue()
    }

    void setCompass(Boolean compass) {
        this.compass = compass
        resetQueue()
    }

    private resetQueue() {
        threadsCount.times {
            jrubies.put(initJruby(compass));
        }
    }

    Map compile(File scssFile, Map params, Collection fullLoadPaths) {
        def jruby = null

        try {
            jruby = jrubies.take()

            //call a method defined in the ruby source
            def c = jruby.container
            def u = jruby.unit
            c.put("@template", scssFile.text);
            c.put("@params", params);
            c.put("@load_paths", fullLoadPaths as CopyOnWriteArrayList)

            return (Map) JavaEmbedUtils.rubyToJava(u.run())
        } finally {
            if (jruby) jrubies.put(jruby)
        }
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

}
