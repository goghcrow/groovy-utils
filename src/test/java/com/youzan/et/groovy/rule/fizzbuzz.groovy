package com.youzan.et.groovy.rule

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

def fizzBuzzRules =
        rules {
            rule {
                order 2
                when { num % 5 == 0 }
                then { println 'fizz' }
            }
            rule {
                order 3
                when { num % 7 == 0 }
                then { println 'buzz' }
            }
            rule {
                order 1
                when { num % 5 == 0 && num % 7 == 0 }
                then { println 'fizzbuzz' }
            }
            rule {
                order 3
                when { num % 5 != 0 && num % 7 != 0 }
                then { println num}
            }
        }


fizzBuzzRules << rule {
    order 0
    when { name == 'xiaofeng42' }
    then { println 'the answer' }
}

def engine = new RuleEngine(skipOnApplied: true)

(1..100).each {
    engine.fire(fizzBuzzRules, [
            num: it,
            name: "xiaofeng$it"
    ])
}

// (1..100).each { println it; println engine.check(fizzBuzzRules, [num: it]) }