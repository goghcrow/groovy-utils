package com.youzan.et.groovy.rulex.dataobj

import groovy.transform.Canonical

@Canonical
class SceneVarDO {
    Long id
    String appId
    Long sceneId
    String sceneCode
    String varName
    String varDesc
}