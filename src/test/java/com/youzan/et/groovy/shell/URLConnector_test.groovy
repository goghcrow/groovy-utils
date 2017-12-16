package com.youzan.et.groovy.shell

def root = "http://gitlab.qima-inc.com/".toURL()
def urlConnector = new URLConnector(root)
def scriptEngine = new GroovyScriptEngine(urlConnector, this.class.classLoader)
println scriptEngine.run("ET/et-script/raw/master/src/test/java/testRBAC.groovy", new Binding());