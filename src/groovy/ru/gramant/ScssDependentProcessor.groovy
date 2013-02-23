/*
 * ScssDependentProcessor
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package ru.gramant

class ScssDependentProcessor {

    private dependOnFiles = [:].withDefault { key -> []}

    void refreshScssFile(File file) {
        if (file.exists()) {
            dependOnFiles[file.canonicalPath] = ScssUtils.getDependOnScssNames(file.text)
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
