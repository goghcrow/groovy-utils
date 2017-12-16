package com.youzan.et.groovy.rbac


class Api implements Permission {
    RBAC rbac
    String id
    String desc
    Set<HttpMethod> methods = []
    Set<String> path = []
    Set<String> exclude = []

    enum HttpMethod {
        GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS, TRACE
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
    def method(String sm) {
        def g = rbac.ofGroup(sm)
        if (g) {
            method g
        } else {
            methods << HttpMethod.valueOf(sm.toUpperCase())
        }
        this
    }
    def method(HttpMethod m) {
        methods << m
        this
    }
    def method(Group g) {
        addMethods g.container
        this
    }
    def method(...ms) {
        addMethods ms
        this
    }
    def path(...ps) {
        ps.each {
            if (it instanceof String || it instanceof GString) {
                path << (it as String)
            }
        }
        this
    }
    def exclude(...ps) {
        ps.each {
            if (it instanceof String || it instanceof GString) {
                exclude << (it as String)
            }
        }
        this
    }

    private addMethods(its) {
        its.each {
            if (it instanceof HttpMethod) {
                method it as HttpMethod
            } else if (it instanceof String || it instanceof GString) {
                method it as String
            } else if (it instanceof Group) {
                addMethods it.container
            }
        }
    }

    @Override
    boolean isPermitted(Object obj) {
        return false
    }

    String toString() {
        "Api(id=$id, methods=$methods, path=$path, exclude=$exclude)"
    }
}
