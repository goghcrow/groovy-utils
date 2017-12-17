package com.youzan.et.groovy.rules

import groovy.transform.CompileStatic

@CompileStatic
interface Condition {
    boolean when(Map<String, Object> facts)
}