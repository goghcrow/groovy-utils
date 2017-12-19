package com.youzan.et.groovy.rule

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode

@CompileStatic
@EqualsAndHashCode
class Rule implements Comparable<Rule> {
    private static long _i = 0
    String _name = 'rule' + _i++
    String _code = ''
    String _desc = ''
    int _priority = 42
    Closure<Boolean> _eval = { println "[$this] To be, or not to be: that is the question."; true }
    Closure _exec = { }

    Rule name(String name) { _name = name; this }
    Rule code(String code) { _code = code; this }
    Rule desc(String desc) { _desc = desc; this }
    Rule order(int priority) { _priority = priority; this }
    Rule when(Closure<Boolean> eval) { _eval = eval; this }
    Rule then(Closure exec) { _exec = exec; this }

    Rules leftShift(final Rule r) { new Rules(rules: new TreeSet<Rule>([this, r])) }
    Rules leftShift(final Rules rs) { rs << this }

    Rules plus(final Rule r) { new Rules(rules: new TreeSet<Rule>([this, r])) }
    Rules plus(final Rules rs) { rs + this }

    void call(Map facts) { _exec(facts) }

    int compareTo(final Rule other) {
        if (_priority < other._priority) -1
        else if(_priority > other._priority) 1
        else _name <=> other._name
    }

    String toString() { "$_name($_priority)" }
}