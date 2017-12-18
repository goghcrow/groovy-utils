package com.youzan.et.groovy.rule

import groovy.transform.CompileStatic

@CompileStatic
class Rules implements Iterable<Rule> {
    Set<Rule> rules = new TreeSet<>()

    // 添加并返回原 Rules
    Rules leftShift(final Rule r) { rules.add r; this }
    Rules leftShift(final Rules rs) { rules.addAll rs; this }
    // Rules rule(...args) { args.each { rule it }; this } // 这里静态编译有问题, 暂时取消
    Rules rule(final Rule r) { rules.add(r); this }
    Rules rule(final Rules rs) { rules.addAll rs; this }
    Rules rule(@DelegatesTo(Rule) Closure c) {
        def rule = new Rule();
        rule.with c
        rules.add rule
        this
    }

    // 添加并返回新 Rules
    Rules plus(final Rule r) { new Rules(rules: rules + [r]) }
    Rules plus(final Rules rs) { new Rules(rules: rules + rs) }

    // 移除并返回原 Rules
    Rules minus(final Rule r) { rules.remove(r); this }
    Rules minus(final Rules rs) { rules.removeAll(rs); this }

    Iterator<Rule> iterator() { rules.iterator() }
    String toString() { '[' + rules.join(", ") + ']' }
}
