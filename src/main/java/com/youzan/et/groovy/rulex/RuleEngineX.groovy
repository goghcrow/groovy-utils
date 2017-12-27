package com.youzan.et.groovy.rulex

import com.youzan.et.groovy.rule.Rule
import com.youzan.et.groovy.rule.RuleEngine
import com.youzan.et.groovy.rule.Rules
import com.youzan.et.groovy.rulex.datasrc.SceneRuleDO
import com.youzan.et.groovy.rulex.datasrc.SceneType
import com.youzan.et.groovy.rulex.datasrc.VarType
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

import javax.sql.DataSource
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

    DataSource dataSource

    String appName

    SceneService sceneService

    private
    Map<String, Scene> scenesTbl = new ConcurrentHashMap()

    private
    static ApplicationContext ctx

    void setApplicationContext(ApplicationContext appCtx) throws BeansException { ctx = appCtx }

    private envCheck() {
        if (!ctx) {
            throw new RuntimeException('请在 application.xml 配置 RuleEngineX Bean')
        }
        if (!appName) {
            throw new RuntimeException('请为 RuleEngineX Bean 配置 appName 属性')
        }
        if (!dataSource) {
            throw new RuntimeException('请为 RuleEngineX Bean 配置 dataSource 属性')
        }
        if (!sceneService) {
            sceneService = new SceneService(sceneDS: new SceneDS(ds: dataSource))
        }
    }

    /**
     * 刷新当前应用规则
     */
    void refresh() {
        envCheck()

        def scenes = sceneService.getScenesByApp(appName).findAll { it.sceneStatus }
        if (!scenes) {
            log.info("未获取到应用($appName)规则场景")
            return
        }

        def rules = sceneService.getRulesByApp(appName)
        rules = sceneService.replaceRulesExpression(rules)
        def actions = sceneService.getActionsByRules(appName, rules)

        scenes.each { scene ->
            String dsl = SceneBuilder.render(scene, rules.findAll { it.sceneId == scene.id }, actions)
            log.info("加载规则\n$dsl")

            try {
                def compiled = SceneBuilder.compile(dsl)
                scenesTbl.put(scene.sceneCode, compiled)
                log.info("场景 ${scene.sceneCode} 加载成功")
            } catch (Exception e) {
                log.error("场景 ${scene.sceneCode} 加载失败, 错误的规则定义\n$dsl" , e)
            }
        }
    }

    /**
     * 开启\关闭\刷新场景规则
     * @param sceneCode
     */
    void refresh(String appName, String sceneCode, boolean enabled) {
        envCheck()

        if (enabled) {
            def dsl = render(appName, sceneCode)
            def scene = compile(dsl)
            if (scene == null) {
                throw new RuntimeException("错误的规则定义:\n$dsl")
            }
            if (scenesTbl.containsKey(sceneCode)) {
                log.warn("场景 $sceneCode 更新")
            } else {
                log.warn("场景 $sceneCode 开启")
            }
            scenesTbl.put(sceneCode, scene)
        } else {
            if (scenesTbl.containsKey(sceneCode)) {
                log.warn("场景 $sceneCode 关闭")
                scenesTbl.remove(sceneCode)
            }
        }
    }

    /**
     * 从数据库读取规则渲染 单条规则DSL
     * @param sceneCode
     * @return
     */
    String render(Long ruleId) {
        envCheck()

        if (ruleId == null) return null
        SceneRuleDO rule = sceneService.getRuleById(ruleId)
        rule = sceneService.replaceRulesExpression([rule]).first()
        def actions = sceneService.getActionsByRule(rule)

        SceneBuilder.render(rule, actions)
    }

    /**
     * 从数据库读取规则渲染 场景DSL
     * @param sceneCode
     * @return
     */
    String render(String appName, String sceneCode) {
        envCheck()

        if (appName == null || appName.isAllWhitespace()
            || sceneCode == null || sceneCode.isAllWhitespace()) return null

        def scene = sceneService.getSceneByAppCode(appName, sceneCode)
        if (scene == null) return null

        def rules = sceneService.getRulesByAppCode(appName, sceneCode)
        rules = sceneService.replaceRulesExpression(rules)
        def actions = sceneService.getActionsByRules(appName, rules)

        SceneBuilder.render(scene, rules.findAll { it.sceneId == scene.id }, actions)
    }

    /**
     * 编译规则定义 DSL
     * @param sceneDefine
     * @return
     */
    @SuppressWarnings("GrMethodMayBeStatic")
    Scene compile(String sceneDefine) {
        envCheck()

        if (sceneDefine == null) return null
        SceneBuilder.compile(sceneDefine)
    }

    /**
     * 执行匹配触发规则
     * @param sceneCode
     * @param facts
     */
    Map<Rule, Object> fire(String sceneCode, Map<String, Object> facts) {
        envCheck()
        sanitizeFacts(appName, sceneCode, facts)

        if (sceneCode == null) return null
        if (!scenesTbl.containsKey(sceneCode)) {
            log.warn("场景 $sceneCode 未定义或未开启")
            return null
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
        envCheck()
        sanitizeFacts(appName, sceneCode, facts)

        if (sceneCode == null) return null
        if (!scenesTbl.containsKey(sceneCode)) {
            log.warn("场景 $sceneCode 未定义或未开启")
            return [:]
        }

        check(scenesTbl.get(sceneCode) as Rules, facts)
    }

    /**
     * 测试 场景执行
     * @param appName
     * @param sceneCode
     * @param facts
     * @param doAction 是否执行 action
     */
    Map<Rule, Boolean> test(String appName, String sceneCode, Map<String, Object> facts, boolean doAction = false) {
        envCheck()
        sanitizeFacts(appName, sceneCode, facts)

        def dsl = render(appName, sceneCode)
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

    // 检查 facts 是否缺失, 类型转换
    private void sanitizeFacts(String appName, String sceneCode, Map<String, Object> facts) {
        Objects.requireNonNull(facts)

        sceneService.getVarsByAppCode(appName, sceneCode).each {
            if (!facts.containsKey(it.varName)) {
                throw new RuntimeException("Fact 缺失: $it.varName: $it.varDesc")
            }

            def fact = facts[(it.varName)]
            try {
                facts[(it.varName)] = VarType.typeOf(it.varType).sanitize(fact)
            } catch (Exception e) {
                log.error('fact cast exception: ' + e.getMessage(), e)
                throw new RuntimeException("Fact 转换错误, 期望: $it.varType, 实际值: $fact")
            }
        }
    }
}
