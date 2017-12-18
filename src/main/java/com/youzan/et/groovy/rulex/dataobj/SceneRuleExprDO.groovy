package com.youzan.et.groovy.rulex.dataobj

import groovy.transform.Canonical

@Canonical
class SceneRuleExprDO {
    Long id
    Long sceneRuleId
    String exprVar
    String exprOp
    String exprVal
}