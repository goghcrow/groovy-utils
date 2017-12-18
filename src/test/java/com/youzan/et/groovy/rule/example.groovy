package com.youzan.et.groovy.rule

import com.youzan.et.groovy.rulex.RuleEngineX


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


println '----------------------------------'

/* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- */

new RuleEngine(skipOnApplied: true).fire(rules {
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

new RuleEngine(skipOnIgnored: true).fire(rules {
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

new RuleEngine(skipOnFailed: true).fire(rules {
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

def engine = new RuleEngine()
engine.beforeEval << {rule, facts -> id != 42 }
engine.beforeEval << {rule, facts -> id != 22 }
def testRules = rules {
    rule {
        when { id % 2 == 0 }
        then { println id }
    }
}
engine.fire(testRules, [id: 2])
engine.fire(testRules, [id: 22])
engine.fire(testRules, [id: 4])
engine.fire(testRules, [id: 42])
println ''
/* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- */

def check = new RuleEngine().&check.curry(rules {
    rule {
        when { id == 42 }
        then { println 'universal answer' }
    }
})

println check([id: 1])
println check([id: 42])
println ''

check = new RuleEngineX().&check.curry('''
scene {
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