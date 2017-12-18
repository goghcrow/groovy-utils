package com.youzan.et.groovy.rulex

import groovy.transform.CompileStatic
import org.springframework.context.ApplicationContext

@CompileStatic
class Facts {
    ApplicationContext ctx
    @Delegate Map<String, Object> facts
    Object make(String name) { ctx.getBean(name) }
}
