package com.youzan.et.groovy.rulex

import com.youzan.et.groovy.rulex.datasrc.SceneActionDO
import com.youzan.et.groovy.rulex.datasrc.SceneDO
import com.youzan.et.groovy.rulex.datasrc.SceneRuleDO
import com.youzan.et.groovy.rulex.datasrc.SceneVarDO
import groovy.transform.CompileStatic


@CompileStatic
class SceneService {
    private String appName

    private SceneDS sceneDS

    List<String> getApps() {
        sceneDS.getApps()
    }

    List<SceneDO> getScenes() {
        sceneDS.getScenesByApp(appName)
    }

    List<SceneDO> getScenesByApp(String appName) {
        sceneDS.getScenesByApp(appName)
    }

    boolean switchScene(String sceneCode, boolean flag) {
        if (sceneCode == null || sceneCode.isAllWhitespace()) return false
        sceneDS.updateSceneStatus(sceneCode, flag ? SceneDO.enabled : SceneDO.disabled)
    }

    SceneDO getSceneByAppCode(String appName, String sceneCode) {
        sceneDS.getSceneByCode(appName, sceneCode)
    }

    SceneRuleDO getRuleById(Long ruleId) {
        if (ruleId == null) return null
        getExpressions([sceneDS.getRuleById(ruleId)]).first()
    }

    List<SceneRuleDO> getRulesByApp(String appName) {
        getExpressions(sceneDS.getRulesByApp(appName))
    }

    List<SceneRuleDO> getRulesByCode(String sceneCode) {
        getRulesByAppCode(appName, sceneCode)
    }

    List<SceneRuleDO> getRulesByAppCode(String appName, String sceneCode) {
        getExpressions(sceneDS.getRulesBySceneCode(appName, sceneCode))
    }

    List<SceneActionDO> getActionsByScene(String sceneCode) {
        sceneDS.getActionsByScene(sceneCode)
    }

    List<SceneActionDO> getActionsByCodes(String codes) {
        def codeList = sceneDS.parserActionCodes(codes)
        sceneDS.getActionsByCodes(codeList)
    }

    List<SceneActionDO> getActionsByRule(SceneRuleDO rule) {
        sceneDS.getActionsByCodes(sceneDS.getActionsCodesByRule(rule))
    }

    List<SceneActionDO> getActionsByRules(List<SceneRuleDO> rules) {
        sceneDS.getActionsByCodes(sceneDS.getActionsCodesByRules(rules))
    }

    List<SceneVarDO> getVarsByAppCode(String appName, String sceneCode) {
        sceneDS.getRuleVarsByAppCode(appName, sceneCode)
    }

    private List<SceneRuleDO> getExpressions(List<SceneRuleDO> rules) {
        def exprIds = sceneDS.getExprIdsByRules(rules)
        def exprs = sceneDS.getRuleExprsByIds(exprIds)
        def vars = sceneDS.getRuleVarsByIds(exprs.collect { it.exprVar })
        def exprTbl = sceneDS.makeExprTable(vars, exprs)
        SceneBuilder.compileExprs(rules, exprTbl)
        rules
    }

    int upsertRule(SceneRuleDO ruleDO) {
        if (ruleDO?.id)
            sceneDS.updateRule(ruleDO)
        else
            sceneDS.insertRule(ruleDO)
    }

    int upsertScene(SceneDO scene) {
        if (scene?.id)
            sceneDS.updateScene(scene)
        else
            sceneDS.insertScene(scene)
    }

    // 已经被关联的不能随便编辑
    int upsertVar(SceneVarDO var) {
        if (var?.id)
            sceneDS.updateVar(var)
        else
            sceneDS.insertVar(var)

    }
}
