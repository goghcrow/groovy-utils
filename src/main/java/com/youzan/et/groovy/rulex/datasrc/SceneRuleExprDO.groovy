package com.youzan.et.groovy.rulex.datasrc

import groovy.transform.Canonical
import groovy.transform.CompileStatic

@Canonical
@CompileStatic
class SceneRuleExprDO extends AbstractDO {
    Long id
    String appId
    Long sceneId
    String sceneCode
    Long exprVar
    String exprOp
    String exprVal
}