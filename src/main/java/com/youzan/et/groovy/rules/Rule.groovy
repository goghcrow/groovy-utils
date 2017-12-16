package com.youzan.et.groovy.rules

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
class Rule implements Comparable<Rule> {
    private static i = 0
    String name = 'rule' + i++
    String desc = ''
    int priority = 42
    Closure<Boolean> eval = { false }
    Closure exec = { }
    Closure onSucc = { }
    Closure onFailed = { }

    Rule name(String name) { this.name = name; this }
    Rule desc(String desc) { this.desc = desc; this }
    Rule order(int priority) { this.priority = priority; this }
    Rule when(Closure<Boolean> eval) { this.eval = eval; this }
    Rule then(Closure exec) { this.exec = exec; this }
    Rule success(Closure onSucc) { this.onSucc = onSucc; this }
    Rule fail(Closure onFailed) { this.onFailed = onFailed; this }

    Rules leftShift(final Rule r) { new Rules(rules: [this, r]) }
    Rules leftShift(final Rules rs) { rs << this }

    Rules plus(final Rule r) { new Rules(rules: [this, r]) }
    Rules plus(final Rules rs) { rs + this }

    void call(Map facts) { exec(facts) }

    int compareTo(final Rule other) {
        if (priority < other.priority) -1
        else if(priority > other.priority) 1
        else name <=> other.name
    }

    String toString() { "$name-$priority" }
}