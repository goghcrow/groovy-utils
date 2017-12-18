package com.youzan.et.groovy.rulex

import com.youzan.et.groovy.rule.Rule
import com.youzan.et.groovy.rule.RuleEngine
import com.youzan.et.groovy.rule.Rules
import com.youzan.et.groovy.rulex.datasrc.SceneDAO
import com.youzan.et.groovy.shell.GShell
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

/**
 * Spring集成
 */
@Slf4j
@CompileStatic
class RuleEngineX extends RuleEngine implements ApplicationContextAware {

    {
        beforeEval << { Rule rule, Map<String, Object> facts ->
            true
        }
        afterEval << { Rule rule, Map<String, Object> facts, boolean trigger ->
            log.info("规则 ${rule._name} ${trigger ? '触发' : '拒绝'}" )
        }
        afterExec << { Rule rule, Map<String, Object> facts, Exception ex ->
            if (ex) {
                log.error("规则 ${rule._name} 执行失败", ex)
            } else {
                log.info("规则 ${rule._name} 执行成功" )
            }
        }
    }

    ApplicationContext ctx
    SceneDAO sceneDAO

    void setApplicationContext(ApplicationContext appCtx) throws BeansException { ctx = appCtx }

    protected Facts makeDelegate(Map<String, Object> facts) {
        facts.putIfAbsent('log', log)
        facts.putIfAbsent('facts', facts)
        new Facts(ctx: ctx, facts: facts)
    }

    void build() {
        Objects.requireNonNull(ctx)
        String appId = ctx.getApplicationName()
        assert appId

        sceneDAO.getScenesByApp(appId)
        sceneDAO.getRulesByApp(appId)

        // TODO
    }

    void fire(String define, Map<String, Object> facts) {
        Objects.requireNonNull(define)

        def rules = evalRules(define)
        if (rules) {
            fire(rules as Rules, facts)
        }
    }

    Map<Rule, Boolean> check(String define, Map<String, Object> facts) {
        Objects.requireNonNull(define)

        def rules = evalRules(define)
        if (rules) {
            check(rules as Rules, facts)
        } else {
            [:]
        }
    }

    @CompileStatic
    abstract static class BaseScript extends Script {
        @SuppressWarnings("GrMethodMayBeStatic")
        Rule rule(@DelegatesTo(Rule) Closure c) {
            def rule = new Rule()
            rule.with c
            rule
        }

        @SuppressWarnings("GrMethodMayBeStatic")
        Scene scene(@DelegatesTo(Rules) Closure c) {
            def _scene = new Scene()
            _scene.with c
            _scene
        }
    }

    private static Rules evalRules(String rules) {
        def shell = new GShell()
        shell.conf.setScriptBaseClass BaseScript.name
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
