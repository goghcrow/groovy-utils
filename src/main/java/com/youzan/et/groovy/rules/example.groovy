package com.youzan.et.groovy.rules

Rule rule(Closure c) {
    def rule = new Rule()
    rule.with c
    rule
}

def rules(Closure c) {
    def rules = new Rules()
    rules.with c
    rules
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
    rule rl, rls, {
        name 'z-rule'
        order 11
        when { Map facts -> true }
        then { Map facts -> println 'exec-z-rule'}
    }
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
        order 1
        when { it.num % 5 == 0 }
        then { println 'fizz' }
    }
    rule {
        order 1
        when { it.num % 8 == 0 }
        then { println 'buzz' }
    }
    rule {
        order 0
        when { it.num % 5 == 0 && it.num % 8 == 0 }
        then { println 'fizzbuzz' }
    }
    rule {
        order 2
        when { it.num % 5 != 0 && it.num % 8 != 0 }
        then { println it.num}
    }
}

def engine = new RuleEngine()
(1..100).each {
    engine.fire(fizzBuzzRules, ['num': it])
}

(1..100).each {
    println engine.check(fizzBuzzRules, ['num': it])
}

/* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- */