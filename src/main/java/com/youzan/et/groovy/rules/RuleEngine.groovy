package com.youzan.et.groovy.rules

import groovy.transform.CompileStatic

@CompileStatic
class RuleEngine {
    boolean skipOnApplied = false
    boolean skipOnIgnored = false
    boolean skipOnFailed = false

    List<Closure<Boolean>> beforeEval = [] // 可用来补全数据
    List<Closure> afterEval = []
    List<Closure> afterExec = []

    void fire(Rules rules, Map facts) {
        def delegate = new Facts(facts)

        rules?.any { rule ->
            if (beforeEval.any { it.delegate = delegate; !it(rule, facts) }) return true

            rule._eval.delegate = delegate
            def trigger = rule._eval.call facts // 这里用 call 避开静态类型检查
            afterEval.each { it.delegate = delegate; it(rule, facts, trigger) }

            if (trigger) {
                try {
                    rule._exec.delegate = delegate
                    rule._exec.call facts // call 同上
                    afterExec.each { it.delegate = delegate; it(rule, facts, null) }
                    if (skipOnApplied) return true
                } catch (Exception e) {
                    afterExec.each { it.delegate = delegate; it(rule, facts, e) }
                    if (skipOnFailed) return true
                }
            } else {
                if (skipOnIgnored) return true
            }

            false
        }
    }

    Map<Rule, Boolean> check(Rules rules, Map facts) {
        def delegate = new Facts(facts)

        rules?.collectEntries() { rule ->
            if (beforeEval.any { it.delegate = delegate; !it(rule, facts) }) return true

            rule._eval.delegate = new Facts(facts)
            [(rule) : rule._eval.call(facts)] // call 同上
        }
    }
}
