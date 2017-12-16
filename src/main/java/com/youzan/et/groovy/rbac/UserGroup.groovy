package com.youzan.et.groovy.rbac

class UserGroup/* implements TraitRoles */{
    RBAC rbac
    String id
    String desc
    List<Role> roles = []
    List<User> users = []

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
    def user(String user) {
        def g = rbac.ofGroup(user)
        if (g) {
            addUsers g.container
        } else {
            users << rbac.ofUser(user)
        }
        this
    }
    def user(User user) {
        users << user
        this
    }
    def user(Group g) {
        addUsers g.container
        this
    }
    def user(...us) {
        addUsers us
        this
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

    private addUsers(its) {
        its.each {
            if (it instanceof User) {
                user it as User
            } else if (it instanceof String || it instanceof GString) {
                user it as String
            } else if (it instanceof Group) {
                addUsers it.container
            }
        }
    }

    String toString() {
        "UserGroup(id=$id, roles=${roles.findAll{ it }.collect{ it.id }}, users=${users.findAll{ it }.collect{ it.id }})"
    }
}
