package com.youzan.et.groovy.rulex.datasrc

import groovy.transform.Canonical
import groovy.transform.CompileStatic

@Canonical
@CompileStatic
class SceneDO {
    final static Byte skipOnApplied = (byte)1
    final static Byte skipOnIgnored = (byte)2
    final static Byte skipOnFailed = (byte)3

    Long id
    String appId
    String sceneName
    String sceneCode
    String sceneDesc
    Byte sceneType
}
