package com.youzan.et.groovy.rules

import groovy.transform.CompileStatic

@CompileStatic
class Scene extends Rules {
    private static long _i = 0
    String _name = 'scene' + _i++
    String _desc = ''

    Scene name(String name) { _name = name; this }
    Scene desc(String desc) { _desc = desc; this }
}
