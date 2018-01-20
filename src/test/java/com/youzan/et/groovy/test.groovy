package com.youzan.et.groovy

import groovyx.gpars.dataflow.Dataflows

import static groovyx.gpars.actor.Actors.*
import static groovyx.gpars.GParsPool.withPool
import static groovyx.gpars.dataflow.Dataflow.task


def exit = System.&exit


assert [1..3].flatten() == [1,2,3]

assert [1..3].value == [[1,2,3]]
assert [1..3]*.value == [[1,2,3]]
assert [1..3].toList() == [1..3]


withPool {
    assert [1,4,9] == [1..3].flatten().makeConcurrent().collect { it * it }
}

withPool {
    assert 55 == [0..4].flatten().parallel
            .map { it + 1 }
            .map { it ** 2 }
            .reduce { a, b -> a + b }
}


withPool {
    assert [1,4,9] == [1..6].flatten().parallel
            .map { it * it }
            .filter { it < 10 }
            .collection
}

withPool {
    assert [1,4,9] == [1..6].flatten().parallelStream()
            .map { it * it }
            .filter { it < 10 }
            .collect()
}

withPool (3) {}


def pid = { println Thread.currentThread().id }
final flow = new Dataflows()
task { pid(); flow.result = flow.x + flow.y }
task { pid(); flow.x = 10 }
task { pid(); flow.y = 5 }
assert 15 == flow.result

final flowDeadlock = new Dataflows()
task { flowDeadlock.x = flowDeadlock.y; println "hello" }
task { flowDeadlock.y = flowDeadlock.x; println "world" }



messageHandler {

}

def decryptor = actor {
    loop {
        react {
            if (it instanceof String) reply it.reverse()
            else stop()
        }
    }
}

def console = actor {
    decryptor << 'lellarap si yvoorG'
    react {
        println 'Decrypted message: ' + it
        decryptor << false
    }
}
[decryptor, console]*.join()




//println worker.dump().replaceAll(' ', '\n')

actor {
    actor {
        react {
            reply it.reverse()
        }
    } << 'hello'
    react {
        println it
    }
}.join()






//@CompileStatic
//class MapX {
//    @Delegate Map map = [hello: 'world']
//}
//
//def m = new MapX()
//println m.hello
//println m.size()





//Runtime.addShutdownHook { println 'hello' }
//
//println Boolean[].name
//println forClassName('java.lang.Integer[]')

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