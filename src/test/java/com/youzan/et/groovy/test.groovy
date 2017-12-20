package com.youzan.et.groovy

import groovy.transform.CompileStatic

//@CompileStatic
//class MapX {
//    @Delegate Map map = [hello: 'world']
//}
//
//def m = new MapX()
//println m.hello
//println m.size()
//
//Runtime.addShutdownHook {
//    println 'hello'
//}

println Boolean[].name
println forClassName('java.lang.Integer[]')

static Class<?> forClassName(String className) {
    def cls = [
            'boolean': boolean.class,
            'byte': byte.class,
            'char': char.class,
            'short': short.class,
            'int': int.class,
            'long': long.class,
            'float': float.class,
            'double': double.class,
            'boolean[]': boolean[].class,
            'byte[]': byte[].class,
            'char[]': char[].class,
            'short[]': short[].class,
            'int[]': int[].class,
            'long[]': long[].class,
            'float[]': float[].class,
            'double[]': double[].class
    ][(className)]
    if (cls) return cls

    def classLoader = Thread.currentThread().contextClassLoader
    try {
        def nName = className
        if (className.size() >= 2 && className[-2..-1] == '[]') {
            nName = "[L${className[0..-3]};"
        }
        Class.forName(nName, true, classLoader)
    } catch (ClassNotFoundException e) {
        if (className.indexOf('.') == -1) {
            try {
                className = "java.lang.$className"
                if (className[-2..-1] == '[]') {
                    className = "[L${className[0..-3]};"
                }
                Class.forName(className, true, classLoader)
            } catch (ClassNotFoundException ignored) {}
        }
        throw e
    }
}