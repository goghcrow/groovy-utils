package com.youzan.et.groovy.rbac

class User {
    RBAC rbac
    String id
    String desc
    List<Role> roles = []
    UserScope scope = UserScope.user

    enum UserScope {
        user, department, business
    }

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
    def role(String roleId) {
        def g = rbac.ofGroup(roleId)
        if (g) {
            role g
        } else {
            roles << rbac.ofRole(roleId)
        }
        this
    }
    def role(Role role) {
        roles << role
        this
    }
    def role(Group g) {
        addRoles g.container
        this
    }
    def role(...roles) {
        addRoles roles
        this
    }
    def scope(UserScope scope) {
        this.scope = scope
    }
    def scope(String scope) {
        try {
            scope = UserScope.valueOf(scope)
        } catch (Exception ignored) { }
    }

    private addRoles(its) {
        its.each {
            if (it instanceof Role) {
                role it as Role
            } else if (it instanceof String || it instanceof GString) {
                role it as String
            } else if (it instanceof Group) {
                addRoles it.container
            }
        }
    }

    @Override
    String toString() {
        "User(id=$id, scope=$scope, roles=$roles)"
    }
}