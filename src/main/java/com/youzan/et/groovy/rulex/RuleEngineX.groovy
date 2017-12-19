package com.youzan.et.groovy.rulex

import com.youzan.et.groovy.rule.Rule
import com.youzan.et.groovy.rule.RuleEngine
import com.youzan.et.groovy.rule.Rules
import com.youzan.et.groovy.rulex.datasrc.SceneDO
import com.youzan.et.groovy.rulex.datasrc.SceneRuleDO
import com.youzan.et.groovy.rulex.datasrc.SceneType
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
            log.info("${trigger ? '触发' : '拒绝'} [${rule._name}]" )
        }
        afterExec << { Rule rule, Map<String, Object> facts, Exception ex ->
            if (ex) {
                log.error("[${rule._name}] 执行失败", ex)
            } else {
                log.info("[${rule._name}] 执行成功" )
            }
        }
    }

    Map<String, Scene> scenesTbl = new ConcurrentHashMap()

    ApplicationContext ctx

    @Resource
    SceneDS sceneDS

    void setApplicationContext(ApplicationContext appCtx) throws BeansException { ctx = appCtx }

    protected Facts makeDelegate(Map<String, Object> facts) {
        facts.putIfAbsent('log', log)
        facts.putIfAbsent('facts', facts)
        new Facts(ctx: ctx, facts: facts)
    }

    void load(Long sceneCode) {
        // TODO
    }

    void load() {
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
            log.info("加载规则\n$dsl")

            try {
                def compiled = SceneBuilder.compile(dsl)
                scenesTbl.put(scene.sceneCode, compiled)
            } catch (Exception e) {
                log.error("错误的规则定义\n$dsl" , e)
            }
        }
    }

    void fire(String sceneCode, Map<String, Object> facts) {
        assert sceneCode
        if (!scenesTbl.containsKey(sceneCode)) {
            throw new RuntimeException("规则 $sceneCode 不存在")
        }

        def scene = scenesTbl.get(sceneCode) as Scene
        def opts = SceneType.toRuleEngineOpts(scene._type)
        fire(scene, facts, opts)
    }

    Map<Rule, Boolean> check(String sceneCode, Map<String, Object> facts) {
        assert sceneCode
        if (!scenesTbl.containsKey(sceneCode)) {
            throw new RuntimeException("规则 $sceneCode 不存在")
        }

        check(scenesTbl.get(sceneCode) as Rules, facts)
    }

    void fireDSL(String define, Map<String, Object> facts, Options opts = null) {
        assert define
        def rules = SceneBuilder.compile(define)
        if (rules) {
            fire(rules as Rules, facts, opts)
        }
    }

    Map<Rule, Boolean> checkDSL(String define, Map<String, Object> facts) {
        assert define

        def rules = SceneBuilder.compile(define)
        if (rules) {
            check(rules as Rules, facts)
        } else {
            [:]
        }
    }
}
