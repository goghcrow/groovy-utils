package com.youzan.et.groovy.rules

import groovy.transform.CompileStatic

@CompileStatic
interface Action {
    void then(Map<String, Object> facts)
}