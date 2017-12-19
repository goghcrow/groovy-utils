package com.youzan.et.groovy.rulex

import com.youzan.et.groovy.rule.Rule
import com.youzan.et.groovy.rule.Rules
import com.youzan.et.groovy.rulex.datasrc.SceneActionDO
import com.youzan.et.groovy.rulex.datasrc.SceneDO
import com.youzan.et.groovy.rulex.datasrc.SceneRuleDO
import com.youzan.et.groovy.shell.GShell
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@CompileStatic
@Slf4j
class SceneBuilder {

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
            def scene = new Scene()
            scene.with c
            scene
        }
    }


    private static String compileActs(String actionCodes, Map<String, SceneActionDO> actTable) {
        assert actionCodes != null
        actionCodes.tokenize(',')
                .findAll { it != null }
                .collect { it.trim() }
                .collect {
            actTable[it]?.action
        }.join('; ')
    }

    static String render(SceneDO scene,
                         List<SceneRuleDO> rules,
                         List<SceneActionDO> actions
    ) {
        Map<String, SceneActionDO> actTbl = actions.collectEntries { [(it.actionCode): it]}

        def sb = new StringBuffer(
                """
scene {
    name '${scene.sceneName.replaceAll("'",'')}'
    code '${scene.sceneCode.replaceAll("'",'')}'
    desc '${scene.sceneDesc.replaceAll("'",'')}'
    type  ${scene.sceneType.byteValue()}
""")
        rules.each {
            sb.append """
    rule {
        name '${it.ruleName.replaceAll("'",'')}'
        code '${it.ruleCode.replaceAll("'",'')}'
        desc '${it.ruleDesc.replaceAll("'",'')}'
        order ${it.priority}
        when { ${it.rule} }
        then { ${compileActs(it.actionsCode, actTbl)} }
    }
    
""" }
        sb.append('}')
        sb.toString()
    }

    static Scene compile(String rules) {
        assert rules
        def shell = new GShell()
        shell.conf.setScriptBaseClass BaseScript.name

        def ret = shell.eval(rules)
        if (ret.getOut()) log.info(ret.getOut())
        if (ret.getRet() instanceof Scene) {
            ret.getRet() as Scene
        } else {
            log.error("错误的规则定义: $rules\nRet: ${ret.getRet()}")
            null
        }
    }
}
