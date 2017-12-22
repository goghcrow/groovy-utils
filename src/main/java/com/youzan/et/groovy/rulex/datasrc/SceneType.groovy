package com.youzan.et.groovy.rulex.datasrc

import groovy.transform.CompileStatic

import com.youzan.et.groovy.rule.RuleEngine

@CompileStatic
enum SceneType {
    skipOnApplied(val: (byte)1, opts: RuleEngine.skipOnApplied, desc: '匹配成功忽略后续'),
    skipOnIgnored(val: (byte)2, opts: RuleEngine.skipOnIgnored, desc: '匹配失败忽略后续'),
    skipOnFailed(val: (byte)3, opts: RuleEngine.skipOnFailed, desc: '执行失败忽略后续')
    Byte val
    RuleEngine.Options opts
    String desc

    final static Map<Byte, SceneType> tlb
    static {
        tlb = values().collectEntries{ [(it.val): it] }
    }

    static RuleEngine.Options toRuleEngineOpts(Byte val) {
        tlb[(val)] ? tlb[(val)].opts :  RuleEngine.none
    }

    static String toDesc(Byte val) {
        tlb[(val)] ? tlb[(val)].desc : '缺省'
    }
}