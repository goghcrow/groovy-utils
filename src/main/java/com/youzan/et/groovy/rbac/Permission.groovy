package com.youzan.et.groovy.rbac

interface Permission {
    boolean isPermitted(Object obj)
}