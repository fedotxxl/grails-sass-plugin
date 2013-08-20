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
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.ReentrantReadWriteLock

@Slf4j
@Singleton
class ScssMultiThreadCompiler implements ScssCompiler {

    private final JRubyPool pool = new JRubyPool()
    private BlockingQueue<DelayedJRuby> jrubies
    private Boolean compass = null
    private Integer threadsCount = -1
    private final lock = new ReentrantReadWriteLock()

    void setCompassAndThreadsCount(Boolean compass, Integer count) {
        try {
            lock.writeLock().lock()

            //return jrubies to pool
            this.jrubies?.each {
                pool.put(this.compass, it)
            }

            this.threadsCount = count
            this.compass = compass
            this.jrubies = new ArrayBlockingQueue<>(count)

            //get new jrubies
            threadsCount.times {
                jrubies.put(pool.get(compass));
            }
        } finally {
            lock.writeLock().unlock()
        }
    }

    Map compile(File scssFile, Map params, Collection fullLoadPaths) {
        def delayedJRuby = null

        try {
            lock.readLock().lock()
            delayedJRuby = jrubies.take()

            //call a method defined in the ruby source
            def jruby = delayedJRuby.getJRuby()
            def c = jruby.container
            def u = jruby.unit
            c.put("@template", scssFile.text);
            c.put("@params", params);
            c.put("@load_paths", fullLoadPaths as CopyOnWriteArrayList)

            return (Map) JavaEmbedUtils.rubyToJava(u.run())
        } finally {
            if (delayedJRuby) jrubies.put(delayedJRuby)
            lock.readLock().unlock()
        }
    }

    private static class JRubyPool {

        private List<DelayedJRuby> scss = new LinkedList<>()
        private List<DelayedJRuby> compass = new LinkedList<>()

        DelayedJRuby get(Boolean compass) {
            def container = getContainer(compass)
            if (container.size() > 0) {
                return container.remove(0)
            } else {
                return new DelayedJRuby(compass)
            }
        }

        void put(Boolean compass, DelayedJRuby jRuby) {
            getContainer(compass).add(jRuby)
        }

        private getContainer(Boolean compass) {
           return (compass) ? this.compass : this.scss
        }

    }

    private static class DelayedJRuby {
        private Boolean compass
        private Map jruby

        DelayedJRuby(Boolean compass) {
            this.compass = compass
        }

        Map getJRuby() {
            if (!jruby) {
                jruby = initJruby(compass)
            }

            return jruby
        }

        private initJruby(Boolean compass) {
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
}
