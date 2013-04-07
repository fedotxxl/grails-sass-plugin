/*
 * ScssDependentProcessor
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package ru.gramant

import groovy.util.logging.Slf4j

@Slf4j
class ScssDependentProcessor {

    private dependOnFiles = [:].withDefault { key -> []}
    private filesByModuleName = [:].withDefault {[] as Set}

    void refreshScssFile(File file) {
        log.trace "SCSS: refreshing dependencies for file [${file}]"

        if (file.exists()) {
            dependOnFiles[file.canonicalPath] = ScssUtils.getDependOnScssNames(file.text)
            filesByModuleName[ScssUtils.getScssName(file.canonicalPath)] << file.canonicalPath
        } else {
            dependOnFiles[file.canonicalPath] = []
        }
    }

    List<File> getDependentFiles(File file) {
        //calculate
        def dependentFiles = []
        calculateDependentFiles(file.canonicalPath, dependentFiles)

        //change and return
        return dependentFiles.collect { new File(it) }
    }

    List<File> getDependsOnFiles(File file) {
        //calculate
        def dependsOn = [] as Set
        calculateDependsOnFiles(file.canonicalPath, dependsOn)

        //change and return
        return dependsOn.collect { new File(it) }.findAll {it.exists()}
    }

    private calculateDependsOnFiles(String path, Set prevDependsOnFiles = []) {
        def dependsOn = dependOnFiles[path].collect { module -> filesByModuleName[module]}.flatten()
        dependsOn.each {
            if (!prevDependsOnFiles.contains(it)) {
                prevDependsOnFiles << it
                calculateDependsOnFiles(it, prevDependsOnFiles)
            }
        }
    }

    private calculateDependentFiles(String path, List prevDependentFiles = []) {
        def answer = []
        def scssName = ScssUtils.getScssName(path)

        dependOnFiles.each {
            //check that we already doesn't have this file
            if (it.key != path && !prevDependentFiles.contains(it.key)) {
                //check is current file (it.key) depends on scssName
                if (it.value.contains(scssName)) {
                    answer << it.key
                }
            }
        }

        //update total list
        prevDependentFiles.addAll(answer);

        //calculate dependencies of added once
        answer.each {
            calculateDependentFiles(it, prevDependentFiles)
        }
    }
}
