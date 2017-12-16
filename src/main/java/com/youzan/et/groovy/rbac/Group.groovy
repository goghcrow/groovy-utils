package com.youzan.et.groovy.rbac

class Group {
    RBAC rbac
    String id
    String desc
    // 这里约定 Group 内为同构元素
    List container = []

    def id(String id) {
        this.id = id
        this
    }
    def id(Integer id) {
        this.id = id as String
        this
    }
    def desc(String d) {
        desc = d
        this
    }
    def api(...apis) {
        add(*apis)
    }
    def role(...roles) {
        add(*roles)
    }
    def user(...users) {
        add(*users)
    }

    private add(...terms) {
        terms.each {
            if (it instanceof Permission || it instanceof String || it instanceof GString || it instanceof Group) {
                container << it
            }
        }
    }

    String toString() {
        if (container) {
            "Group(id=$id, $container)"
        } else {
            "Group(id=$id, [])"
        }
    }
}