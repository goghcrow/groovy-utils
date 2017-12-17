package com.youzan.et.groovy.rules

import groovy.transform.CompileStatic

@CompileStatic
abstract class RuleBaseScript extends Script {
    Rule rule(@DelegatesTo(Rule) Closure c) {
        def rule = new Rule()
        rule.with c
        rule
    }

    Rules rules(@DelegatesTo(Rules) Closure c) {
        def rules = new Rules()
        rules.with c
        rules
    }
}