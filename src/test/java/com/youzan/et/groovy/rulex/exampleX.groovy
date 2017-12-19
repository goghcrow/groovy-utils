package com.youzan.et.groovy.rulex

import com.youzan.et.groovy.rule.Rule
import com.youzan.et.groovy.rule.Rules
import org.springframework.context.support.StaticApplicationContext

import java.lang.annotation.Annotation

static Rule rule(@DelegatesTo(Rule) Closure c) {
    def _rule = new Rule()
    _rule.with c
    _rule
}

static Rules rules(@DelegatesTo(Rules) Closure c) {
    def _rules = new Rules()
    _rules.with c
    _rules
}

static Rules scene(@DelegatesTo(Rules) Closure c) {
    def _scene = new Scene()
    _scene.with c
    _scene
}

def exit = System.&exit

def engineX() {
    def ctx = new StaticApplicationContext()
    ctx.registerSingleton('sceneDAO', SceneDS)
    ctx.registerSingleton('ruleEngineX', RuleEngineX)
    ctx.registerSingleton('helloServ', HelloService)
    ctx.registerSingleton('mailServ', EmailService)
    ctx.refresh()
    ctx.getBean('ruleEngineX') as RuleEngineX
}
/* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- */


//println "({0} && ({1} || {2}))".findAll(/\{\d+\}/).collect { it[1..-2] as Long }
//println "({0} && ({1} || {2}))".replaceAll(/\{\d+\}/) {
//    [0: 'hello', 1: 'world', 2: 'xxx'][it[1..-2] as Integer]
//}

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
x.load()

x.fire('scene_test', [id: 999])

println ''
println 999
println x.check('scene_test', [id: 999])
println 1
println x.check('scene_test', [id: 1])
println 42
println x.check('scene_test', [id: 42])





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


Biz.getDeclaredFields().each {
    it.getAnnotation(FactField.class)
}
//println Utils.findAnnotation(FactField.class, Biz.getDeclaredField('name').class)

//def anno = Utils.findAnnotation(Fact.class, Biz)
//anno.name()


/* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- */
