package com.youzan.et.groovy.shell

import org.springframework.context.ApplicationContext

abstract class BaseScript extends Script {
    // 注意: 这里是惰性获取 ctx 的, 不能直接改写成 java
    // Java: public ApplicationContext getCtx()
    @Delegate @Lazy ApplicationContext ctx = this.binding.ctx
    def invokeMethod(String name, args) {
        getBinding().ctx."${name.toLowerCase()}"(*args)
    }
}
