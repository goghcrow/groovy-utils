package com.youzan.et.groovy.rulex

import com.youzan.et.groovy.rule.Rule
import com.youzan.et.groovy.rule.RuleEngine
import com.youzan.et.groovy.rule.Rules
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

    @Resource
    SceneDS sceneDS

    ApplicationContext ctx

    void setApplicationContext(ApplicationContext appCtx) throws BeansException { ctx = appCtx }

    /**
     * 刷新当前应用规则
     */
    void refresh() {
        assert ctx
        String appId = ctx.getApplicationName()
        // TODO
        appId = 'et_xiaolv'
        assert appId

        // TODO
        if (!sceneDS) sceneDS = new SceneDS()

        def scenes = sceneDS.getScenesByApp(appId)
        if (!scenes) return

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

    /**
     * 刷新场景规则
     * @param sceneCode
     */
    void refresh(String sceneCode) {
        def dsl = render(sceneCode)
        def scene = compile(dsl)
        if (scene == null) {
            throw new RuntimeException("错误的规则定义:\n$dsl")
        }
        scenesTbl.put(scene._code, scene)
    }

    /**
     * 从数据库读取规则渲染 DSL
     * @param sceneCode
     * @return
     */
    String render(String sceneCode) {
        if (sceneCode == null) return null

        assert ctx
        String appId = ctx.getApplicationName()
        // TODO
        appId = 'et_xiaolv'
        assert appId

        // TODO
        if (!sceneDS) sceneDS = new SceneDS()

        def scene = sceneDS.getSceneByCode(appId, sceneCode)
        if (scene == null) return null

        def rules = sceneDS.getRulesBySceneCode(appId, sceneCode)
        def actCodes = sceneDS.getActionsCodesByRules(rules)
        def actions = sceneDS.getActionsByCodes(actCodes)
        def exprIds = sceneDS.getExprIdsByRules(rules)
        def exprs = sceneDS.getRuleExprsByIds(exprIds)
        def vars = sceneDS.getRuleVarsByIds(exprs.collect { it.exprVar })
        def exprTbl = sceneDS.makeExprTable(vars, exprs)
        sceneDS.compileExprs(rules, exprTbl)

        return SceneBuilder.render(scene, rules.findAll { it.sceneId == scene.id }, actions)
    }

    /**
     * 编译规则定义 DSL
     * @param sceneDefine
     * @return
     */
    @SuppressWarnings("GrMethodMayBeStatic")
    Scene compile(String sceneDefine) {
        if (sceneDefine == null) return null
        SceneBuilder.compile(sceneDefine)
    }

    /**
     * 执行匹配触发规则
     * @param sceneCode
     * @param facts
     */
    void fire(String sceneCode, Map<String, Object> facts) {
        assert sceneCode
        if (!scenesTbl.containsKey(sceneCode)) {
            throw new RuntimeException("场景未定义(code=$sceneCode)")
        }

        def scene = scenesTbl.get(sceneCode) as Scene
        def opts = SceneType.toRuleEngineOpts(scene._type)
        fire(scene, facts, opts)
    }

    /**
     * 不执行结果, 返回规则匹配结果
     * @param sceneCode
     * @param facts
     * @return
     */
    Map<Rule, Boolean> check(String sceneCode, Map<String, Object> facts) {
        assert sceneCode
        if (!scenesTbl.containsKey(sceneCode)) {
            throw new RuntimeException("场景未定义(code=$sceneCode)")
        }

        check(scenesTbl.get(sceneCode) as Rules, facts)
    }

    /**
     * 测试 场景执行
     * @param sceneCode
     * @param facts
     * @param doAction 是否执行 action
     */
    Map<Rule, Boolean> test(String sceneCode, Map<String, Object> facts, boolean doAction = false) {
        def dsl = render(sceneCode)
        if (dsl == null) {
            throw new RuntimeException("场景未定义(code=$sceneCode)")
        }
        def scene = compile(dsl)
        if (scene == null) {
            throw new RuntimeException("错误的规则定义\n$dsl")
        }
        if (doAction) {
            def opts = SceneType.toRuleEngineOpts(scene._type)
            fire(scene, facts, opts)
        }
        check(scene, facts)
    }

    protected Facts makeDelegate(Map<String, Object> facts) {
        facts.putIfAbsent('log', log)
        facts.putIfAbsent('facts', facts)
        new Facts(ctx: ctx, facts: facts)
    }
}
