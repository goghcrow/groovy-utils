package com.youzan.et.groovy.rulex

import com.youzan.et.groovy.rulex.datasrc.SceneActionDO
import com.youzan.et.groovy.rulex.datasrc.SceneDO
import com.youzan.et.groovy.rulex.datasrc.SceneRuleDO
import groovy.transform.CompileStatic

@CompileStatic
class SceneService {
    private SceneDS sceneDS
    private String appName

    List<SceneDO> getSceneList() {
        sceneDS.getScenesByApp(appName)
    }

    List<SceneRuleDO> getRulesBySceneCode(String sceneCode) {
        sceneDS.getRulesBySceneCode(appName, sceneCode)
    }

    boolean createNewScene(SceneDO scene) {
        assert scene.sceneName != null
        assert scene.sceneCode != null
        assert scene.sceneDesc != null
        assert scene.sceneType != null
        scene.appId = appName
        sceneDS.insertScene(scene)
    }
}
