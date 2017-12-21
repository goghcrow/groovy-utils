package com.youzan.et.groovy.rulex.datasrc

import groovy.transform.Canonical
import groovy.transform.CompileStatic

@Canonical
@CompileStatic
class SceneActionDO extends AbstractDO {
    Long id
    String appId
    Long sceneId
    String sceneCode
    String actionCode
    String actionDesc
    String action
}
