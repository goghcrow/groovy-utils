package com.youzan.et.groovy.rbac

class Role {
    RBAC rbac
    String id
    String desc
    List<Permission> permissions = []

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
    def api(Permission perm) {
        permissions << perm
        this
    }
    def api(String id) {
        def g = rbac.ofGroup(id)
        if (g) {
            api g
        } else {
            permissions << rbac.ofApi(id)
        }
        this
    }
    def api(Group g) {
        addPerms g.container
        this
    }
    def api(...apis) {
        addPerms apis
        this
    }

    private addPerms(its) {
        its.each {
            if (it instanceof Permission) {
                api it as Permission
            } else if (it instanceof String || it instanceof GString) {
                api it as String
            } else if (it instanceof Group) {
                addPerms it.container
            }
        }
    }

    String toString() {
        "Role(id=$id, perms=${permissions.findAll { it }.collect { "${it.class.simpleName}(${it.id})" }})"
    }
}