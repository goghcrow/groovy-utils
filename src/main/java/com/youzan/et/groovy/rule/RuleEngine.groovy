package com.youzan.et.groovy.rule

import groovy.transform.CompileStatic

@CompileStatic
class RuleEngine {
    @CompileStatic
    static class Options {
        boolean skipOnApplied = false
        boolean skipOnIgnored = false
        boolean skipOnFailed = false
    }

    final static Options none = new Options()
    final static Options skipOnApplied = new Options(skipOnApplied: true)
    final static Options skipOnIgnored = new Options(skipOnIgnored: true)
    final static Options skipOnFailed = new Options(skipOnFailed: true)

    List<Closure<Boolean>> beforeEval = [] // e.g. 数据补全
    List<Closure> afterEval = []
    List<Closure> afterExec = []

    protected Object makeDelegate(Map<String, Object> facts) { facts }

    /**
     * 触发规则
     * @param rules
     * @param facts
     */
    void fire(Rules rules, Map<String, Object> facts, Options opts = null) {
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
                    if (opts?.skipOnApplied) return true
                } catch (Exception e) {
                    afterExec.each { it.delegate = delegate; it(rule, facts, e) }
                    if (opts?.skipOnFailed) return true
                }
            } else {
                if (opts?.skipOnIgnored) return true
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
