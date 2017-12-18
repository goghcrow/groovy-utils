package com.youzan.et.groovy.rulex.dataobj

import groovy.transform.Canonical

@Canonical
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
