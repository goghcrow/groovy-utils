package com.youzan.et.groovy.rulex

import com.youzan.et.groovy.rulex.datasrc.*
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.transform.PackageScope

import javax.sql.DataSource

//@CompileStatic
@PackageScope
class SceneDS {

    // static { GroovyRowResult.metaClass.toBean = this.&toBean }

    // @Resource
    DataSource ds

    // TODO
    private Sql db() {
        // new Sql(ds)
        Sql.newInstance('jdbc:mysql://127.0.0.1:3306/et_engine?useServerPrepStmts=false&zeroDateTimeBehavior=convertToNull&characterEncoding=utf8',
                'root', '123456', 'com.mysql.jdbc.Driver'
        )
    }

    List<SceneDO> getScenesByApp(String appId) {
        assert appId != null
        def rows = db().rows("""
SELECT * FROM et_scene WHERE app_id = $appId
""")
        toBeans(rows, SceneDO)
    }

    SceneDO getSceneByCode(String appId, String sceneCode) {
        assert appId != null
        assert sceneCode != null
        def row = db().firstRow("""
SELECT * FROM et_scene WHERE app_id = $appId and scene_code = $sceneCode LIMIT 1
""")
        toBean(row, SceneDO)
    }

    List<SceneRuleDO> getRulesByApp(String appId) {
        assert appId != null
        def rows = db().rows("""
SELECT * FROM et_scene_rule WHERE app_id = $appId 
""")
        toBeans(rows, SceneRuleDO)
    }

    List<SceneRuleDO> getRulesBySceneCode(String appId, String sceneCode) {
        assert appId != null
        assert sceneCode != null
        def rows = db().rows("""
SELECT * FROM et_scene_rule WHERE app_id = $appId and scene_code = $sceneCode
""")
        toBeans(rows, SceneRuleDO)
    }

    List<SceneActionDO> getActionsByCodes(List<String> codes) {
        if (!codes) return []
        def rows = db().rows("""
SELECT * FROM et_scene_action WHERE action_code IN (${codes.collect{'?'}.join(',')})
""", codes)
        toBeans(rows, SceneActionDO)
    }

    List<SceneRuleExprDO> getRuleExprsByIds(List<Long> ids) {
        if (!ids) return []
        def rows = db().rows("""
SELECT * FROM et_scene_rule_expr WHERE id IN (${ids.collect {'?'}.join(',')})
""", ids)
        toBeans(rows, SceneRuleExprDO)
    }

    List<SceneVarDO> getRuleVarsByIds(List<Long> ids) {
        if (!ids) return []
        def rows = db().rows("""
SELECT * FROM et_scene_var WHERE id IN (${ids.collect{'?'}.join(',')})
""", ids)
        toBeans(rows, SceneVarDO)
    }

    static List<String> getActionsCodesByRules (List<SceneRuleDO> rules) {
        if (!rules) return []
        rules.collect{
            it.actionsCode.tokenize(',').findAll { it != null }.collect { it.trim() }
        }.flatten().unique() as List<String>
    }

    static List<Long> getExprIdsByRules(List<SceneRuleDO> rules) {
        if (!rules) return []
        rules.findAll { it.ruleType == SceneRuleDO.expr }.collect {
            it.rule.findAll(/\{\d+\}/).collect { it[1..-2] as Long }
        }.flatten().unique() as List<Long>
    }

    static Map<Long, String> makeExprTable(List<SceneVarDO> vars, List<SceneRuleExprDO> exprs) {
        Map<Long, SceneVarDO> varMap = vars.collectEntries { [(it.id): it] }
        exprs.collectEntries {
            assert varMap[it.exprVar] // TODO op: in?
            [(it.id): "${varMap[it.exprVar]?.varName} ${it.exprOp} ${it.exprVal}"]
        }
    }

    static void compileExprs(List<SceneRuleDO> rules, Map<Long, String> exprTable) {
        rules.each {
            if (it.ruleType == SceneRuleDO.expr) {
                it.rule = it.rule.replaceAll(/\{\d+\}/) {
                    assert exprTable[it[1..-2] as Long]
                    exprTable[it[1..-2] as Long]
                }
            }
        }
    }

    static <T> T toBean(GroovyRowResult row, Class<T> kind) {
        if (row == null) return null
        def bean = kind.newInstance()
        row.each {
            def k = (it.key as String).replaceAll(/_\w/){ strs ->
                (strs[1] as String).toUpperCase()
            }
            bean[k] = it.value
        }
        bean
// 静态编译有问题
//        row.inject(kind.newInstance(), { T ins, Map.Entry<String, Object> entry ->
//            ins[(entry.key as String).replaceAll(/_\w/){ List<String> strs ->
//                (strs[1] as String).toUpperCase()
//            }] = entry.value
//            ins
//        })
    }

    static <T> List<T> toBeans(List<GroovyRowResult> rows, Class<T> kind) {
        rows.collect { row -> toBean(row, kind) } as List<T>
    }
}
