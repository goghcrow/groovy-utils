package com.youzan.et.groovy.rulex

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource
import com.youzan.et.groovy.rule.Rule
import com.youzan.et.groovy.rule.Rules
import org.springframework.context.support.StaticApplicationContext


def exit = System.&exit


static Rule rule(@DelegatesTo(Rule) Closure c) {
    def rule = new Rule()
    rule.with c
    rule
}

static Rules rules(@DelegatesTo(Rules) Closure c) {
    def rules = new Rules()
    rules.with c
    rules
}

static Rules scene(@DelegatesTo(Rules) Closure c) {
    def scene = new Scene()
    scene.with c
    scene
}


class HelloService {
    @SuppressWarnings("GrMethodMayBeStatic")
    void sayHello() { println 'hello' }
}

class EmailService {
    @SuppressWarnings("GrMethodMayBeStatic")
    boolean send(Map<String, Object> facts) {
        println ">>> Send email $facts"
        true
    }
}

static def engineX() {
    def ds = new MysqlDataSource()
    ds.url = 'jdbc:mysql://127.0.0.1:3306/et_engine?useServerPrepStmts=false&zeroDateTimeBehavior=convertToNull&characterEncoding=utf8'
    ds.user = 'root'
    ds.password = '123456'

    def ctx = new StaticApplicationContext()
    ctx.registerSingleton('ruleEngineX', RuleEngineX)
    ctx.registerSingleton('helloServ', HelloService)
    ctx.registerSingleton('mailServ', EmailService)
    ctx.refresh()
    def x = ctx.getBean('ruleEngineX') as RuleEngineX

    x.dataSource = ds
    x.appName = 'et_xiaolv'

    x
}

/* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- */


def x = engineX()
x.refresh()

x.fire('scene_test', [id: 999])

println ''
println 999
println x.check('scene_test', [id: 999])
println 1
println x.check('scene_test', [id: 1])
println 42
println x.check('scene_test', [id: 42])


println ''
println ''
println ''
//def dsl = x.render('scene_test')
//println dsl
//println x.compile(dsl)

//x.refresh('scene_test')

println x.test('et_xiaolv', 'scene_test', [id: 42], false)

exit(1)

engineX().fire(scene {
    rule {
        then {
            log.info 'logging...'
            println "id=$id name=$name"
            make('helloServ').sayHello()
            make('mailServ').send(facts)
        }
    }
}, [id: 42, name: 'xiaofeng'])

println ''