/*
 * ScssUtilsSpec
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package ru.gramant

import org.springframework.core.io.ClassPathResource

class ScssUtilsSpec extends spock.lang.Specification  {

    def "test getDependOnScssNames"() {
        expect:
        assert ScssUtils.getDependOnScssNames(new ClassPathResource(filePath).file.text) == (expectedDenedencies as Set)

        where:
        filePath | expectedDenedencies
        "/ru/gramant/dependencies.scss" | ["b", "hello world", "f", "super_simple", "first", "second", "with.scss", "with.sass"]
    }

    def "test scss names extraction"() {
        expect:
        assert ScssUtils.getScssName(filePath) == expectedScssName

        where:
        filePath | expectedScssName
        "./abc" | "abc"
        "c:/abc" | "abc"
        "c:\\abc" | "abc"
        "c:/abc.txt" | "abc"
        "c:\\abc.txt" | "abc"
        "c:\\abc\\efd.txt" | "efd"
        "c:/abc/efd.txt" | "efd"
        "c:/hello world/abc efd.txt" | "abc efd"
        "c:\\hello world\\abc efd.txt" | "abc efd"
    }

    def "test comments cleaner"() {
        expect:
        assert ScssUtils.removeCommentsFromScss(new ClassPathResource(source).file.text) == new ClassPathResource(target).file.text

        where:
        source | target
        "/ru/gramant/1.withComments.scss" | "/ru/gramant/1.withoutComments.scss"
    }
}
