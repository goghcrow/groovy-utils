package com.youzan.et.groovy.rules

Rule rule(@DelegatesTo(Rule) Closure c) {
    def _rule = new Rule()
    _rule.with c
    _rule
}

Rules rules(@DelegatesTo(Rules) Closure c) {
    def _rules = new Rules()
    _rules.with c
    _rules
}

/* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- */



/* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- */
println '构建一个 Rule'
println rule {
    name 'x-rule'   // String
    desc 'x-rule-desc'     // String
    order 42 // int, 数字越小 优先级越高
    when { Map facts -> true } // boolean when(Map facts)
    then { Map facts -> println 'exec-x-rule' } // void then(Map facts)
}
println ''
/* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- */
println '构建一个 Rules'
def rl = rule { }
def rls = rules { }
println rules {
    // Rule 类型参数, 添加单条 Rule 到 Rules
    rule rl
    // Rules 类型参数, 添加多条 Rule 到 Rules
    rule rls
    // 支持 Closure 类型声明, 添加单条 Rule 到 Rules
    rule {
        name 'y-rule'
        order 1
        when { Map facts -> true }
        then { Map facts -> println 'exec-y-rule' }
    }

    // 支持不定参数, 支持 Rule|Rules|Closure 三种类型任意个数不定参数
//    rule rl, rls, {
//        name 'z-rule'
//        order 11
//        when { Map facts -> true }
//        then { Map facts -> println 'exec-z-rule'}
//    }
}
println ''
/* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- */
println '单条Rule 到 Rules'
println rules {} << rule {}
println  '添加多条 Rule 到 Rules'
println rules {} << rules { rule { } rule { }}
println ''
/* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- */
// 使用 << 添加 Rule | Rules
println "Rule << Rule 得到 Rules"
println rule { } << rule { }
println "Rule << Rules 往 Rules 添加 Rule"
println rule { } << rules { rule { } }
println "Rules << Rule 往 Rules 添加 Rule"
println rules { rule { } } << rule { }
println "Rules << Rules 往 Rules 添加多天 Rule"
println rules { rule { } } << rules { rule { } }
println rules { rule { } } << rule { } << rules { rule { } } << rule {}
println ''
/* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- */
println 'Rule + Rule 得到 Rules'
println rule {} + rule {}
println 'Rule + Rules 得到 新的 Rules'
println rule {} + rules { rule {} }
/* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- */
println 'Rules + Rule 得到 新的 Rules'
println rules { rule { } } + rule { }
println 'Rules + Rules 得到 新的 Rules'
println rules { rule { } } + rules { rule { } }
println ''
/* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- */
println 'Rules - Rule  移除该 Rule'
def rm = rule {}
println rules {} + rm - rm
/* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- */


def fizzBuzzRules =

rules {
    rule {
        order 2
        when { num % 5 == 0 }
        then { println 'fizz' }
    }
    rule {
        order 3
        when { num % 7 == 0 }
        then { println 'buzz' }
    }
    rule {
        order 1
        when { num % 5 == 0 && num % 7 == 0 }
        then { println 'fizzbuzz' }
    }
    rule {
        order 3
        when { num % 5 != 0 && num % 7 != 0 }
        then { println it.num}
    }
}



fizzBuzzRules << rule {
    order 0
    when { name == 'xiaofeng42' }
    then { println 'hello' }
}

def engine = new RuleEngine(skipOnApplied: true)

(1..100).each {
    engine.fire(fizzBuzzRules, [
            num: it,
            name: "xiaofeng$it"
    ])
}

// (1..100).each { println it; println engine.check(fizzBuzzRules, [num: it]) }
println ''
System.exit 1
/* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- */

new RuleEngineEx(skipOnApplied: true).fire(rules {
    rule {
        when { id == 42 }
        then { println 42 }
    }
    rule {
        when { id % 2 == 0}
        then { assert false }
    }
}, [id: 42])
println ''

/* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- */

new RuleEngineEx(skipOnIgnored: true).fire(rules {
    rule {
        when { id != 42 }
        then { println 42 }
    }
    rule {
        when { id % 2 == 0 }
        then { assert false }
    }
}, [id: 42])
println ''

/* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- */

new RuleEngineEx(skipOnFailed: true).fire(rules {
    rule {
        when { id == 42 }
        then { throw new RuntimeException() }
    }
    rule {
        when { id % 2 == 0 }
        then { assert false }
    }
}, [id: 42])
println ''

/* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- */

def engineEx = new RuleEngineEx()
engineEx.beforeEval << {rule, facts -> id != 42 }
engineEx.beforeEval << {rule, facts -> id != 22 }
def testRules = rules {
    rule {
        when { id % 2 == 0 }
        then { println id }
    }
}
engineEx.fire(testRules, [id: 2])
engineEx.fire(testRules, [id: 22])
engineEx.fire(testRules, [id: 4])
engineEx.fire(testRules, [id: 42])
println ''
/* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- */

def check = new RuleEngineEx().&check.curry(rules {
    rule {
        when { id == 42 }
        then { println 'universal answer' }
    }
})

println check([id: 1])
println check([id: 42])
println ''

check = new RuleEngineEx().&check.curry('''
rules {
    rule {
        when { id == 42 }
        then { println 'universal answer' }
    }
}
''')

println check([id: 1])
println check([id: 42])
println ''

/* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- */

def codeGen(Map<String, String> define) {
    def sb = new StringBuffer()
    "rules {\n" + define.inject(sb) { StringBuffer it, entry -> it.append """
    rule {
        when { $entry.key }
        then { $entry.value }
    }
""" + "\n}"
    }
}



new RuleEngineEx().fire(
        codeGen(['id == 42': 'println "hello"']),
        [id: 42]
)


/* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- */

@Fact(name ='业务对象')
class Biz {

    @FactField(name='姓名')
    String name

    Integer id
}


Biz.getDeclaredFields().each {
    it.getAnnotation(FactField.class)
}
println Utils.findAnnotation(FactField.class, Biz.getDeclaredField('name').class)

//def anno = Utils.findAnnotation(Fact.class, Biz)
//anno.name()