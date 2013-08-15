package ru.gramant.compilers

/*
 * ScssCompiler
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */

public interface ScssCompiler {

    void setCompass(Boolean compass)

    Map compile(File scssFile, Map params, Collection fullLoadPaths)

}