/*
 * JrubyTest
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package ru.gramant

import javax.script.ScriptEngine
import javax.script.ScriptEngineFactory
import javax.script.ScriptEngineManager
import org.junit.Test

class JRubyTest {

    /*BSFManager manager;

    public static void main(String[] args) throws Exception {
        JRubyTest jruby = new JRubyTest();
        jruby.eval("puts 'hello from a string of ruby code'");
        File f = new File("ruby/test.rb");
        jruby.exec(f);
        jruby.eval("$out.println('hello System.out')");
    }

    public JRubyTest() throws BSFException {
        BSFManager.registerScriptingEngine("ruby", "org.jruby.javasupport.bsf.JRubyEngine", new String[] { "rb" });
        manager = new BSFManager();
        manager.declareBean("out", System.out, PrintStream.class);
    }

    public Object eval(String script) throws BSFException {
        return manager.eval("ruby", "(java)", 1, 1, script);
    }

    public void exec(File file) throws BSFException, IOException {
        FileReader in = new FileReader(file);
        String script = IOUtils.getStringFromReader(in);
        manager.exec("ruby", file.getName(), 1, 1, script);
    }*/

    @Test
    void testSingleScss() {
        ScriptEngine jruby = new ScriptEngineManager().getEngineByName("jruby");
        //process a ruby file
        jruby.eval(new BufferedReader(new InputStreamReader(CommonUtils.getClassPathResource("myscss.rb"))));

        //call a method defined in the ruby source
        jruby.put("template", "html { body { font-size: 15px } }");
        jruby.put("params", ['syntax': 'scss', 'style': 'compressed']);


        String compiled = (String) jruby.eval("compileSingleScss(\$template, \$params)");
        println compiled
    }


//    @Test
    void testScss() {
        ScriptEngine jruby = new ScriptEngineManager().getEngineByName("jruby");
        //process a ruby file
        jruby.eval(new BufferedReader(new InputStreamReader(CommonUtils.getClassPathResource("myscss.rb"))));

        //call a method defined in the ruby source
        jruby.put("filesIn", "D:/Dropbox/gramant/projects/test/grails-sass-mine-plugin/web-app/scss/[^_]*.scss");
        jruby.put("filesTo", "D:/Dropbox/gramant/projects/test/grails-sass-mine-plugin/web-app/css");

        jruby.eval("compileScss(\$filesIn, \$filesTo)");
    }

//    @Test
    void testJRuby() {
        listScriptingEngines()

        ScriptEngine jruby = new ScriptEngineManager().getEngineByName("jruby");
        //process a ruby file
        jruby.eval(new BufferedReader(new InputStreamReader(CommonUtils.getClassPathResource("myruby.rb"))));

        //call a method defined in the ruby source
        jruby.put("n", 6);

        long fact = (Long) jruby.eval("fact(\$n)");
        System.out.println("fact: " + fact);
    }

    public static void listScriptingEngines() {
        ScriptEngineManager mgr = new ScriptEngineManager();
        for (ScriptEngineFactory factory : mgr.getEngineFactories()) {
            System.out.println("ScriptEngineFactory Info");
            System.out.printf("\tScript Engine: %s (%s)\n", factory.getEngineName(), factory.getEngineVersion());
            System.out.printf("\tLanguage: %s (%s)\n", factory.getLanguageName(), factory.getLanguageVersion());
            for (String name : factory.getNames()) {
                System.out.printf("\tEngine Alias: %s\n", name);
            }
        }
    }

}
