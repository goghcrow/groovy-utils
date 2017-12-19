package com.youzan.et.groovy.rulex.datasrc

import groovy.transform.Canonical
import groovy.transform.CompileStatic

@Canonical
@CompileStatic
class SceneDO extends AbstractDO {
    Long id
    String appId
    String sceneName
    String sceneCode
    String sceneDesc
    Byte sceneType
}
