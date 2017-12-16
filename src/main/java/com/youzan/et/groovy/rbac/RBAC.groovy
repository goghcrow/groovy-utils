package com.youzan.et.groovy.rbac

import groovy.transform.Canonical

@Canonical
class RBAC {
    List<Permission> permissions = []
    List<Role> roles = []
    List<User> users = []
    List<UserGroup> userGroups = []
    List<Group> groups = []

    Permission ofApi(String id) {
        permissions.find { it && it.id == id }
    }

    Role ofRole(String id) {
        roles.find { it && it.id == id }
    }

    Group ofGroup(String id) {
        groups.find { it && it.id == id }
    }

    User ofUser(String id) {
        def user = users.find { it && it.id == id }
        if (!user) {
            user = new User(id: id)
            users << user
        }
        user
    }

    def api(String id, Closure c) {
        def api = new Api(rbac: this, id: id)
        permissions << api
        api.with c
        api
    }

    def api(Closure c) {
        def api = new Api(rbac: this)
        permissions << api
        api.with c
        api
    }

    def role(String id, Closure c) {
        def role = new Role(rbac: this, id: id)
        roles << role
        role.with c
        role
    }

    def role(Closure c) {
        def role = new Role(rbac: this)
        roles << role
        role.with c
        role
    }

    def user(String id, Closure c) {
        def user = new User(rbac: this, id: id)
        users << user
        user.with c
        user
    }

    def user(Closure c) {
        def user = new User(rbac: this)
        users << user
        user.with c
        user
    }

    def users(String id, Closure c) {
        def ug = new UserGroup(rbac: this, id: id)
        userGroups << ug
        ug.with c
        ug
    }

    def users(Closure c) {
        def ug = new UserGroup(rbac: this)
        userGroups << ug
        ug.with c
        ug
    }

    def group(String id, Closure c) {
        def g = new Group(rbac: this, id: id)
        groups << g
        g.with c
        g
    }

    def group(Closure c) {
        def g = new Group(rbac: this)
        groups << g
        g.with c
        g
    }

    String toString() {
        """
*Permissions*
${permissions.collect {  "\n\t$it" }}

*Roles*
${roles.collect { "\n\t$it" }}

*Users*
${users.collect { "\n\t$it" }}

*Groups*
${groups.collect { "\n\t$it" }}

*UserGroups*
${userGroups.collect { "\n\t$it" }}
"""
    }
}