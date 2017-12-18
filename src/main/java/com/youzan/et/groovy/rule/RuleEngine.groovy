package com.youzan.et.groovy.rule

import groovy.transform.CompileStatic

@CompileStatic
class RuleEngine {

    boolean skipOnApplied = false
    boolean skipOnIgnored = false
    boolean skipOnFailed = false

    List<Closure<Boolean>> beforeEval = [] // e.g. 数据补全
    List<Closure> afterEval = []
    List<Closure> afterExec = []

    protected Object makeDelegate(Map<String, Object> facts) { facts }

    /**
     * 触发规则
     * @param rules
     * @param facts
     */
    void fire(Rules rules, Map<String, Object> facts) {
        def delegate = makeDelegate(facts)

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

    /**
     * 检测规则
     * @param rules
     * @param facts
     * @return
     */
    Map<Rule, Boolean> check(Rules rules, Map<String, Object> facts) {
        def delegate = makeDelegate(facts)

        rules?.collectEntries() { rule ->
            if (beforeEval.any { it.delegate = delegate; !it(rule, facts) }) return true

            rule._eval.delegate = delegate
            [(rule) : rule._eval.call(facts)] // call 同上
        }
    }
}
