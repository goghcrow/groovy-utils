package com.youzan.et.groovy.rulex.datasrc

import groovy.transform.Canonical
import groovy.transform.CompileStatic

@Canonical
@CompileStatic
class SceneDO extends AbstractDO {
    final static Byte disabled = (byte)0
    final static Byte enabled = (byte)1

    Long id
    String appId
    String sceneName
    String sceneCode
    String sceneDesc
    Byte sceneType
    Byte sceneStatus
}
