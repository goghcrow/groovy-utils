package com.youzan.et.groovy.rulex.datasrc

import groovy.transform.Canonical
import groovy.transform.CompileStatic

@Canonical
@CompileStatic
class SceneRuleExprDO {
    Long id
    Long sceneRuleId
    String exprVar
    String exprOp
    String exprVal
}