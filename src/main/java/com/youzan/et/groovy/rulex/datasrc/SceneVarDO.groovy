package com.youzan.et.groovy.rulex.datasrc

import groovy.transform.Canonical
import groovy.transform.CompileStatic

@Canonical
@CompileStatic
class SceneVarDO extends AbstractDO {
    Long id
    String appId
    Long sceneId
    String sceneCode
    String varName
    String varType
    String varDesc
}