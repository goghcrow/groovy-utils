package com.youzan.et.groovy.rulex

import com.youzan.et.groovy.rulex.datasrc.OperatorType
import com.youzan.et.groovy.rulex.datasrc.SceneActionDO
import com.youzan.et.groovy.rulex.datasrc.SceneDO
import com.youzan.et.groovy.rulex.datasrc.SceneRuleDO
import com.youzan.et.groovy.rulex.datasrc.SceneRuleExprDO
import com.youzan.et.groovy.rulex.datasrc.SceneVarDO
import groovy.transform.CompileStatic


@CompileStatic
class SceneService {
    @Delegate
    private SceneDS sceneDS

    @SuppressWarnings("GroovyUnusedDeclaration")
    List<SceneActionDO> getActionsByCodes(String appName, String codes) {
        def codeList = sceneDS.parserActionCodes(codes)
        sceneDS.getActionsByCodes(appName, codeList)
    }

    List<SceneActionDO> getActionsByRule(SceneRuleDO rule) {
        def codeList = sceneDS.getActionsCodesByRule(rule)
        sceneDS.getActionsByCodes(rule.appId, codeList)
    }

    List<SceneActionDO> getActionsByRules(String appName, List<SceneRuleDO> rules) {
        def codeList = sceneDS.getActionsCodesByRules(rules)
        sceneDS.getActionsByCodes(appName, codeList)
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    int upsertRule(SceneRuleDO ruleDO) {
        if (ruleDO?.id) {
            sceneDS.updateRule(ruleDO)
        } else {
            sceneDS.insertRule(ruleDO)
        }
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    int upsertScene(SceneDO scene) {
        if (scene?.id) {
            sceneDS.updateScene(scene)
        } else {
            sceneDS.insertScene(scene)
        }
    }

    // 已经被关联的不能随便编辑
    @SuppressWarnings("GroovyUnusedDeclaration")
    int upsertVar(SceneVarDO var) {
        if (var?.id) {
            sceneDS.updateVar(var)
        } else {
            sceneDS.insertVar(var)
        }
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    int upsertAction(SceneActionDO action) {
        if (action?.id) {
            sceneDS.updateAction(action)
        } else {
            sceneDS.insertAction(action)
        }
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    int upsertExpr(SceneRuleExprDO expr) {
        if (expr?.id) {
            sceneDS.updateExpr(expr)
        } else {
            sceneDS.insertExpr(expr)
        }
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    Map<Long, String> compileExprByIds(List<Long> exprIds) {
        def exprs = sceneDS.getExprsByIds(exprIds)
        def vars = sceneDS.getRuleVarsByIds(exprs.collect { it.exprVar })
        compileExprByVars(vars, exprs)
    }

    List<SceneRuleDO> replaceRulesExpression(List<SceneRuleDO> rules, Map<Long, Map> parsed = null) {
        def exprIds = sceneDS.getExprIdsByRules(rules)
        def exprs = sceneDS.getExprsByIds(exprIds)
        def vars = sceneDS.getRuleVarsByIds(exprs.collect { it.exprVar })
        def exprTbl = compileExprByVars(vars, exprs)
        if (parsed != null) {
            parsed << parseExprs(rules, exprTbl)
        }
        compileExprs(rules, exprTbl)
        rules
    }

    private static void compileExprs(List<SceneRuleDO> rules, Map<Long, String> exprTbl) {
        rules.each { SceneRuleDO rule ->
            if (rule.ruleType == SceneRuleDO.expr) {
                /*
                IDE 类型推导有问题 !!!
                "{0} && {1}".aceAll(/\{\d+\}/) { String it -> println it} // {0} {1}
                "{0} && {1}".replaceAll(/\{\d+\}/) { String[] it -> println it} // [{0}] [{1}]
                */
                // rule.rule = rule.rule.replaceAll(/\{\d+\}/) { String it-> exprTbl[it[1..-2] as Long] }
                rule.rule = rule.rule.replaceAll(/\{(\d+)\}/) { List<String> it->
                    assert it.size() == 2
                    exprTbl[it[1] as Long]
                }
            }
        }
    }

    private static Map<Long, Map> parseExprs(List<SceneRuleDO> rules, Map<Long, String> exprTbl) {
        /* IDE 类型推导有问题, 忽略 */
        rules.findAll{ it.ruleType == SceneRuleDO.expr }
                .collectEntries { SceneRuleDO rule ->
            [(rule.id): rule.rule.findAll(~/(?:\{(\d+)\}){1}\s*([&\|]{2})?/) { List<String> it->
                assert exprTbl[it[1] as Long]
                [
                        id: it[1],
                        expr: exprTbl[it[1] as Long],
                        conj: it[2]
                ]
            }]
        }
    }

    private static Map<Long, String> compileExprByVars(List<SceneVarDO> vars, List<SceneRuleExprDO> exprs) {
        Map<Long, SceneVarDO> varMap = vars.collectEntries { [(it.id): it] }
        exprs.collectEntries {
            assert varMap[it.exprVar]

            def varName = varMap[it.exprVar].varName
            def varType = varMap[it.exprVar].varType
            def compiled = OperatorType.ofOp(it.exprOp).compile(varName, varType, it.exprVal)

            [(it.id): compiled]
        }
    }

}
