package com.youzan.et.groovy.rulex

import groovy.sql.GroovyRowResult
import groovy.sql.Sql


static <T> T toBean(GroovyRowResult row, Class<T> kind) {
    row.inject(kind.newInstance(), { T ins, Map.Entry<String, Object> entry ->
        ins[entry.key.replaceAll(/_\w/){ strs ->
            (strs[1] as String).toUpperCase()
        }] = entry.value
        ins
    })
}


static <T> List<T> toBeans(List<GroovyRowResult> rows, Class<T> kind) {
    rows.collect { toBean(it, kind) } as List<T>
}

def selectStar(table, clazz) {
    def db = Sql.newInstance(url: 'jdbc:mysql://127.0.0.1:3306/et_engine?useServerPrepStmts=false&zeroDateTimeBehavior=convertToNull&characterEncoding=utf8',
            user: 'root', password: '123456', driverClassName: 'com.mysql.jdbc.Driver'
    )


    println ''
    def rows = db.rows('SELECT * FROM ' + table)
    println toBeans(rows, clazz)
    println ''
}

//selectStar('et_scene', SceneDO)
//selectStar('et_scene_action', SceneActionDO)
//selectStar('et_scene_rule', SceneRuleDO)
//selectStar('et_scene_rule_expr', SceneRuleExprDO)
//selectStar('et_scene_var', SceneVarDO)


def dao = new SceneDS()
println dao.getScenesByApp('et_xiaolv')
println dao.getRulesByApp('et_xiaolv')
