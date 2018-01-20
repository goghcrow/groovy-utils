package com.youzan.et.groovy.gctest

import com.youzan.et.groovy.shell.GShell


def test1() {
    def shell = new GroovyShell()

    while (true) {
        println shell.evaluate('def add(a,b) {a+b};add(1,2)')
    }
}

def test2() {
    def shell = new GroovyShell(new GroovyClassLoader())

    while (true) {
        println shell.evaluate('def add(a,b) {a+b};add(1,2)')
    }
}

def test3() {
    def cl = new GroovyClassLoader()


    while (true) {
        println cl.parseClass('def add(a,b) {a+b};add(1,2)').newInstance().run()
    }
}

def test4() {
    while (true) {
        def cl = new GroovyClassLoader()
        println cl.parseClass('def add(a,b) {a+b};add(1,2)').newInstance().run()
    }
}

def test5() {
    while (true) {
        println GroovyClassLoaderUtils.loadClass('def add(a,b) {a+b};add(1,2)').newInstance().run()
    }
}

def test6() {
    def gshell = new GShell()
    while (true) {
        println gshell.eval('def add(a,b) {a+b};add(1,2)').ret
    }
}

def test7() {
    def gshell = new GShell()
    0x7fffffff.times {
        println gshell.eval("def add(a,b) {a+b};add(1,$it)").ret
    }
}

System.in.read()
// test1()
// test2()
// test3()
// test4()
// test5()
// test6()
test7()