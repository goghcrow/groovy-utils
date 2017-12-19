package com.youzan.et.groovy.rulex.datasrc

import groovy.transform.CompileStatic

import static com.youzan.et.groovy.rule.RuleEngine.*

@CompileStatic
enum SceneType {
    skipOnApplied(val: (byte)1, opts: skipOnApplied),
    skipOnIgnored(val: (byte)2, opts: skipOnIgnored),
    skipOnFailed(val: (byte)3, opts: skipOnFailed)
    Byte val
    Options opts

    final static Map<Byte, Options> tlb
    static {
        tlb = values().collectEntries{ [(it.val): it.opts] }
    }

    static Options toRuleEngineOpts(Byte val) {
        tlb[(val)] ?: none
    }
}