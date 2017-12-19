package com.youzan.et.groovy.rulex.datasrc

import groovy.transform.Canonical
import groovy.transform.CompileStatic

@Canonical
@CompileStatic
class SceneRuleDO extends AbstractDO {
    final static Byte expr = (byte)1
    final static Byte script = (byte)2

    Long id
    String appId
    Long sceneId
    String sceneCode
    String rule
    Byte ruleType
    String ruleCode
    String ruleName
    String ruleDesc
    Integer priority
    String actionsCode
}
