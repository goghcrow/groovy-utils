package com.youzan.et.groovy.rbac

import com.youzan.et.base.api.UserService
import com.youzan.et.base.api.model.UserModel


def rbac = new RBACService()
rbac.setProperty('userService', new UserService() {
    @Override
    Map<Integer, UserModel> getUserMapByIds(Integer[] integers) {
        return null
    }

    @Override
    List<UserModel> getAllUsers() {
        [
                [
                        casUsername: 'chuxiaofeng',
                        departmentId: 1
                ] as UserModel
        ]
    }
})

rbac.setProperty('uri', 'http://gitlab.qima-inc.com/ET/et-script/raw/master/src/main/java/com/youzan/et/xiaolv/rbac.groovy')
rbac.invokeMethod('refresh', null)


println "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-"
(rbac.getProperty('defaultRole') as Set<Api>).each { it ->
    println "defaultRole"
    it.each {
        println "\t$it"
    }
}
(rbac.getProperty('userRoles') as Map<String, Set<Api>>).each { k,v ->
    println k
    v.each {
        println "\t$it"
    }
}