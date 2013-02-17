/*
 * ScssDependentProcessorSpec
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package ru.gramant

import org.springframework.core.io.ClassPathResource

class ScssDependentProcessorSpec extends spock.lang.Specification  {

    def "test scss names extraction"() {
        expect:
        def processor = new ScssDependentProcessor()
        assert processor.getScssName(filePath) == expectedScssName

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

    def "test getDependOnScssNames"() {
        expect:
        def processor = new ScssDependentProcessor()
        assert processor.getDependOnScssNames(new ClassPathResource(filePath).file.text) == (expectedDenedencies as Set)

        where:
        filePath | expectedDenedencies
        "/ru/gramant/simpleDependencies.scss" | ["b", "hello world", "f", "super_simple"]
    }

}
