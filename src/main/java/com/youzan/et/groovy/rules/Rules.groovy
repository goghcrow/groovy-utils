package com.youzan.et.groovy.rules


class Rules implements Iterable<Rule> {
    Set<Rule> rules = new TreeSet<>()

    // add return origin
    Rules leftShift(final Rule r) { rules.add r; this }
    Rules leftShift(final Rules rs) { rules.addAll rs; this }
    Rules rule(...args) { args.each { rule it }; this }
    Rules rule(final Rule r) { rules.add(r); this }
    Rules rule(final Rules rs) { rules.addAll rs; this }
    Rules rule(Closure c) {
        def rule = new Rule();
        rule.with c
        rules.add rule
        this
    }

    // add return new
    Rules plus(final Rule r) { new Rules(rules: rules + [r]) }
    Rules plus(final Rules rs) { new Rules(rules: rules + rs) }

    // remove return origin
    Rules minus(final Rule r) { rules.remove(r); this }
    Rules minus(final Rules rs) { rules.removeAll(rs); this }

    Iterator<Rule> iterator() { rules.iterator() }
    String toString() { '[' + rules.join(", ") + ']' }
}
