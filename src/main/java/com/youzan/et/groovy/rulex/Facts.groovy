package com.youzan.et.groovy.rulex

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.context.ApplicationContext

/**
 * 提供 表达式 make 获取服务对象, 执行 Action
 * 注意: application.xml 需要配置 RuleEngineX Bean
 */
@CompileStatic
@Slf4j
class Facts {
    ApplicationContext ctx
    @Delegate Map<String, Object> facts /*  implements Map  */
    def <T> T make(String name) { (T)ctx.getBean(name) }
    def <T> T make(Class<T> kind) { ctx.getBean(kind) }
    def <T> T make(String name, Class<T> kind) { ctx.getBean(name, kind) }
}
