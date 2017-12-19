package com.youzan.et.groovy.rulex.datasrc

import groovy.transform.Canonical
import groovy.transform.CompileStatic

@Canonical
@CompileStatic
class SceneActionDO extends AbstractDO {
    Long id
    String actionCode
    String actionDesc
    String action
}
