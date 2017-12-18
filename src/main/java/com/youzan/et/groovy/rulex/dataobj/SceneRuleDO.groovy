package com.youzan.et.groovy.rulex.dataobj

import groovy.transform.Canonical

@Canonical
class SceneRuleDO {
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
