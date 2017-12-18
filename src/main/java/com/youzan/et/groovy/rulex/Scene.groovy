package com.youzan.et.groovy.rulex

import com.youzan.et.groovy.rule.Rules
import groovy.transform.CompileStatic

@CompileStatic
class Scene extends Rules {
    private static long _i = 0
    String _name = 'scene' + _i++
    String _code = ''
    String _desc = ''

    Scene name(String name) { _name = name; this }
    Scene code(String code) { _code = code; this }
    Scene desc(String desc) { _desc = desc; this }
}
