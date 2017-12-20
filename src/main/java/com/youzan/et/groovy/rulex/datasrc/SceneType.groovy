package com.youzan.et.groovy.rulex.datasrc

import groovy.transform.CompileStatic

import static com.youzan.et.groovy.rule.RuleEngine.*

@CompileStatic
enum SceneType {
    skipOnApplied(val: (byte)1, opts: skipOnApplied, desc: '当一条匹配成功忽略后续'),
    skipOnIgnored(val: (byte)2, opts: skipOnIgnored, desc: '当一条匹配失败忽略后续'),
    skipOnFailed(val: (byte)3, opts: skipOnFailed, desc: '当一条执行失败忽略后续')
    Byte val
    Options opts
    String desc

    final static Map<Byte, SceneType> tlb
    static {
        tlb = values().collectEntries{ [(it.val): it] }
    }

    static Options toRuleEngineOpts(Byte val) {
        tlb[(val)] ? tlb[(val)].opts :  none
    }

    static String toDesc(Byte val) {
        tlb[(val)] ? tlb[(val)].desc : '缺省'
    }
}