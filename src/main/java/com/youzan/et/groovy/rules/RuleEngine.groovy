package com.youzan.et.groovy.rules

import groovy.util.logging.Slf4j

@Slf4j
class RuleEngine {
    boolean skipOnApplied = false
    boolean skipOnPassed = false
    boolean skipOnFailed = false

    void fire(Rules rules, Map facts) {
        rules.any {
            if (it.eval(facts)) {
                try {
                    it.exec facts
                    if (skipOnApplied) return true
                } catch (Exception e) {
                    log.error(e.getMessage(), e)
                    if (skipOnFailed) return true
                }
            } else {
                if (skipOnPassed) return true
            }
            return false
        }
    }

    Map<Rule, Boolean> check(Rules rules, Map facts) {
        rules.collectEntries() { [it : it.eval(facts)] }
    }
}
