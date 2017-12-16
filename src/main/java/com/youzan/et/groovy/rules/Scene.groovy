package com.youzan.et.groovy.rules

class Scene extends Rules {
    private static int i = 0
    String name = 'scene' + i++
    String desc = ''

    Scene name(String name) { this.name = name; this }
    Scene desc(String desc) { this.desc = desc; this }
}
