package com.youzan.et.groovy.rules

import com.youzan.et.groovy.shell.GShell
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@Slf4j
@CompileStatic
class RuleEngineEx extends RuleEngine {
    {
        beforeEval << { Rule rule, Map facts ->
            true
        }
        afterEval << { Rule rule, Map facts, boolean trigger ->
            log.info("规则 ${rule._name} ${trigger ? '触发' : '拒绝'}" )
        }
        afterExec << { Rule rule, Map facts, Exception ex ->
            if (ex) {
                log.error("规则 ${rule._name} 执行失败", ex);
            } else {
                log.info("规则 ${rule._name} 执行成功" );
            }
        }
    }

    void fire(String define, Map facts) {
        Objects.requireNonNull(define)

        def rules = evalRules(define)
        if (rules) {
            fire(rules as Rules, facts)
        }
    }

    Map<Rule, Boolean> check(String define, Map facts) {
        Objects.requireNonNull(define)

        def rules = evalRules(define)
        if (rules) {
            check(rules as Rules, facts)
        } else {
            [:]
        }
    }

    private static Rules evalRules(String rules) {
        def shell = new GShell()
        shell.conf.setScriptBaseClass  RuleBaseScript.name
        def ret = shell.eval(rules)
        if (ret.getOut()) log.info(ret.getOut())
        if (ret.getRet() instanceof Rules) {
            ret.getRet() as Rules
        } else {
            log.error("错误的规则定义: $rules\nRet: ${ret.getRet()}")
            null
        }
    }
}
