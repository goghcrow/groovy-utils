package com.youzan.et.groovy.rulex

import com.youzan.et.groovy.rulex.dataobj.SceneActionDO
import com.youzan.et.groovy.rulex.dataobj.SceneDO
import com.youzan.et.groovy.rulex.dataobj.SceneRuleDO
import com.youzan.et.groovy.rulex.dataobj.SceneRuleExprDO
import com.youzan.et.groovy.rulex.dataobj.SceneVarDO
import groovy.sql.GroovyRowResult
import groovy.sql.Sql

static <T> List<T> rowResult2DO(List<GroovyRowResult> rows, Class<T> doClazz) {
    rows.collect { row ->
        row.inject(doClazz.newInstance(), { ins, it ->
            ins[(it.key as String).replaceAll(/_\w/){ (it[1] as String).toUpperCase() }] = it.value
            ins
        })
    } as List<T>
}

def jdbc = 'jdbc:mysql://127.0.0.1:3306/et_engine?useServerPrepStmts=false&zeroDateTimeBehavior=convertToNull&characterEncoding=utf8'
def user = 'root'
def pwd = '123456'
def driver = 'com.mysql.jdbc.Driver'

db = Sql.newInstance(jdbc, user, pwd, driver)

def selectStar(table, clazz) {
    println ''
    def rows = db.rows('SELECT * FROM ' + table)
    println rows
    println rowResult2DO(rows, clazz)
    println ''
}

selectStar('et_scene', SceneDO)
selectStar('et_scene_action', SceneActionDO)
selectStar('et_scene_rule', SceneRuleDO)
selectStar('et_scene_rule_expr', SceneRuleExprDO)
selectStar('et_scene_var', SceneVarDO)