package com.youzan.et.groovy.rules

import groovy.transform.CompileStatic

@CompileStatic
class Facts {
    Map _props = [:]
    Facts(Map props) { if (props) this._props = props }

    def getProperty(String prop){ return _props[prop] }
    void setProperty(String prop, val){ _props[prop] = val }
}
