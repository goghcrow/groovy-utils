package com.youzan.et.groovy.rulex.datasrc

import groovy.transform.Canonical
import groovy.transform.CompileStatic

import java.sql.Timestamp


@Canonical
@CompileStatic
class AbstractDO {
    Timestamp createdAt
    Timestamp updatedAt
    Timestamp deletedAt
}
