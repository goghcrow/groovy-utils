package com.youzan.et.groovy.rulex

import com.youzan.et.groovy.rule.Rule
import com.youzan.et.groovy.rule.Rules
import com.youzan.et.groovy.rulex.datasrc.SceneActionDO
import com.youzan.et.groovy.rulex.datasrc.SceneDO
import com.youzan.et.groovy.rulex.datasrc.SceneRuleDO
import com.youzan.et.groovy.shell.GShell
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.util.logging.Slf4j

@CompileStatic
@Slf4j
@PackageScope
class SceneBuilder {

    @CompileStatic
    abstract static class BaseScript extends Script {
        @SuppressWarnings("GrMethodMayBeStatic")
        Rule rule(@DelegatesTo(Rule) Closure c) {
            def rule = new Rule()
            rule.with c
            rule
        }

        @SuppressWarnings(["GrMethodMayBeStatic", "GroovyUnusedDeclaration"])
        Scene scene(@DelegatesTo(Scene) Closure c) {
            def scene = new Scene()
            scene.with c
            scene
        }
    }

    private static String compileActs(String actionCodes, Map<String, SceneActionDO> actTable) {
        assert actionCodes != null
        '\n\t\t\t\t' + actionCodes.tokenize(',')
                .findAll { it != null }
                .collect { it.trim() }
                .collect {
            actTable[it]?.action
        }.join(';\n\t\t\t\t') + '\n\t\t'
    }

    static String render(SceneRuleDO rule, List<SceneActionDO> actions) {
        Map<String, SceneActionDO> actTbl = actions.collectEntries { [(it.actionCode): it]}
"""rule {
    name '${rule.ruleName.replaceAll("'",'\\\'')}'
    code '${rule.ruleCode.replaceAll("'",'\\\'')}'
    desc '${rule.ruleDesc.replaceAll("'",'\\\'')}'
    order ${rule.priority}
    when { ${rule.rule} }
    then { ${compileActs(rule.actionsCode, actTbl)} }
}
"""
    }

    static String render(SceneDO scene,
                         List<SceneRuleDO> rules,
                         List<SceneActionDO> actions
    ) {
        Map<String, SceneActionDO> actTbl = actions.collectEntries { [(it.actionCode): it]}

        def sb = new StringBuffer(
                """scene {
    name '${scene.sceneName.replaceAll("'",'\\\'')}'
    code '${scene.sceneCode.replaceAll("'",'\\\'')}'
    desc '${scene.sceneDesc.replaceAll("'",'\\\'')}'
    type  ${scene.sceneType.byteValue()}
""")
        rules.each {
            sb.append """
    rule {
        name '${it.ruleName.replaceAll("'",'\\\'')}'
        code '${it.ruleCode.replaceAll("'",'\\\'')}'
        desc '${it.ruleDesc.replaceAll("'",'\\\'')}'
        order ${it.priority}
        when { ${it.rule} }
        then { ${compileActs(it.actionsCode, actTbl)} }
    }
    
""" }
        sb.append('}')
        sb.toString()
    }

    static Scene compile(String rules) {
        if (rules == null) return null

        def shell = new GShell()
        shell.conf.setScriptBaseClass BaseScript.name

        def r = shell.eval(rules)
        if (r.out) log.info(r.out)

        if (r.ret instanceof Scene) {
            r.ret as Scene
        } else if(r.ret instanceof Exception) {
            throw r.ret as Exception
        } else {
            throw new RuntimeException("错误的规则定义")
        }
    }
}
