package com.youzan.et.groovy.rulex

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource
import com.youzan.et.groovy.rule.Rule
import com.youzan.et.groovy.rule.Rules
import com.youzan.et.groovy.rulex.doc.Fact
import com.youzan.et.groovy.rulex.doc.FactField
import org.springframework.context.support.StaticApplicationContext

import java.lang.annotation.Annotation

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

//println "({0} && ({1} || {2}))".findAll(/\{\d+\}/).collect { it[1..-2] as Long }
//println "({0} && ({1} || {2}))".replaceAll(/\{\d+\}/) {
//    [0: 'hello', 1: 'world', 2: 'xxx'][it[1..-2] as Integer]
//}


/* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- */


class HelloService {
    void sayHello() { println 'hello' }
}

class EmailService {
    boolean send(Map<String, Object> facts) {
        println ">>> Send email $facts"
        true 
    }
}

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


/* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- */

@Fact(name ='业务对象')
class Biz {

    @FactField(name='姓名')
    String name

    Integer id
}


class Utils {
    static <A extends Annotation> A findAnnotation(final Class<A> targetAnnotation, final Class<?> annotatedType) {
        A foundAnnotation = annotatedType.getAnnotation(targetAnnotation);
        if (foundAnnotation == null) {
            for (Annotation annotation : annotatedType.getAnnotations()) {
                Class<? extends Annotation> annotationType = annotation.annotationType();
                if (annotationType.isAnnotationPresent(targetAnnotation)) {
                    foundAnnotation = annotationType.getAnnotation(targetAnnotation);
                    break;
                }
            }
        }
        return foundAnnotation;
    }

    static boolean isAnnotationPresent(final Class<? extends Annotation> targetAnnotation, final Class<?> annotatedType) {
        return findAnnotation(targetAnnotation, annotatedType) != null;
    }
}


/* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- */
