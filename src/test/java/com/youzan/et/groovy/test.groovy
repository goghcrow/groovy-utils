package com.youzan.et.groovy

import groovy.transform.CompileStatic

@CompileStatic
class MapX {
    @Delegate Map map = [hello: 'world']
}

def m = new MapX()
println m.hello
println m.size()

Runtime.addShutdownHook {
    println 'hello'
}