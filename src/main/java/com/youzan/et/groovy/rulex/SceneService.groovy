package com.youzan.et.groovy.rulex

import com.youzan.et.groovy.rulex.datasrc.SceneActionDO
import com.youzan.et.groovy.rulex.datasrc.SceneDO
import com.youzan.et.groovy.rulex.datasrc.SceneRuleDO
import groovy.transform.CompileStatic

import javax.sql.DataSource

@CompileStatic
class SceneService {
    private String appName

    private SceneDS sceneDS

    List<SceneDO> getScenes() {
        sceneDS.getScenesByApp(appName)
    }

    List<SceneDO> getScenesByApp(String appName) {
        sceneDS.getScenesByApp(appName)
    }

    SceneDO getSceneByAppCode(String appName, String sceneCode) {
        sceneDS.getSceneByCode(appName, sceneCode)
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

    List<SceneActionDO> getActions(List<SceneRuleDO> rules) {
        def actCodes = sceneDS.getActionsCodesByRules(rules)
        return sceneDS.getActionsByCodes(actCodes)
    }

    private List<SceneRuleDO> getExpressions(List<SceneRuleDO> rules) {
        def exprIds = sceneDS.getExprIdsByRules(rules)
        def exprs = sceneDS.getRuleExprsByIds(exprIds)
        def vars = sceneDS.getRuleVarsByIds(exprs.collect { it.exprVar })
        def exprTbl = sceneDS.makeExprTable(vars, exprs)
        SceneBuilder.compileExprs(rules, exprTbl)
        rules
    }

    // TODO
    boolean createNewScene(SceneDO scene) {
        assert scene.sceneName != null
        assert scene.sceneCode != null
        assert scene.sceneDesc != null
        assert scene.sceneType != null
        scene.appId = appName
        sceneDS.insertScene(scene)
    }

}
