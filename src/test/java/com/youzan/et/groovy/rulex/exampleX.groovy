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

def ex = new RuleEngineX()
def ctx = new StaticApplicationContext()
ctx.registerSingleton('helloServ', HelloService)
ctx.registerSingleton('mailServ', EmailService)
ex.ctx = ctx

ex.fire(scene {
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


def codeGen(Map<String, String> define) {
    def sb = new StringBuffer()
    "scene {\n" + define.inject(sb) { StringBuffer it, entry -> it.append """
    rule {
        when { $entry.key }
        then { $entry.value }
    }
""" + "\n}"
    }
}

new RuleEngineX().fire(
        codeGen(['id == 42': 'println "hello"']),
        [id: 42]
)
exit(1)


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
