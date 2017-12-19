package com.youzan.et.groovy.rulex

import com.youzan.et.groovy.rule.Rule
import com.youzan.et.groovy.rule.RuleEngine
import com.youzan.et.groovy.rule.Rules
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

import javax.annotation.Resource
import java.util.concurrent.ConcurrentHashMap

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

    Map<Long, Scene> scenesTable = new ConcurrentHashMap()

    ApplicationContext ctx

    @Resource
    SceneDS sceneDS

    void setApplicationContext(ApplicationContext appCtx) throws BeansException { ctx = appCtx }

    protected Facts makeDelegate(Map<String, Object> facts) {
        facts.putIfAbsent('log', log)
        facts.putIfAbsent('facts', facts)
        new Facts(ctx: ctx, facts: facts)
    }


    void build() {
        assert ctx
        String appId = ctx.getApplicationName()
        // TODO
        appId = 'et_xiaolv'
        assert appId

        // TODO
        if (!sceneDS) sceneDS = new SceneDS()

        def scenes = sceneDS.getScenesByApp(appId)
        def rules = sceneDS.getRulesByApp(appId)
        def actCodes = sceneDS.getActionsCodesByRules(rules)
        def actions = sceneDS.getActionsByCodes(actCodes)
        def exprIds = sceneDS.getExprIdsByRules(rules)
        def exprs = sceneDS.getRuleExprsByIds(exprIds)
        def vars = sceneDS.getRuleVarsByIds(exprs.collect { it.exprVar })

        def exprTbl = sceneDS.makeExprTable(vars, exprs)
        sceneDS.compileExprs(rules, exprTbl)

        scenes.each { scene ->
            String dsl = SceneBuilder.render(scene, rules.findAll { it.sceneId == scene.id }, actions)
            println dsl
            def x = SceneBuilder.compile(dsl)
            println x
        }

    }



    void fire(String define, Map<String, Object> facts, Options opts = null) {
        assert define

        def rules = SceneBuilder.compile(define)
        if (rules) {
            fire(rules as Rules, facts, opts)
        }
    }

    Map<Rule, Boolean> check(String define, Map<String, Object> facts) {
        assert define

        def rules = SceneBuilder.compile(define)
        if (rules) {
            check(rules as Rules, facts)
        } else {
            [:]
        }
    }
}
