package ru.gramant
/*
 * CommonUtils
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */

class CommonUtils {

    static InputStream getClassPathResource(path) {
        return this.classLoader.getResourceAsStream(path)
    }

}
