package com.youzan.et.groovy.rulex

import com.youzan.et.groovy.rulex.datasrc.*
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.transform.CompileStatic

import javax.sql.DataSource

@CompileStatic
class SceneDS {
    private Sql db

    SceneDS(DataSource ds) {
        db = new Sql(ds)
    }

    void init() {
        db.execute(getClass().getResource('/rulex/init.sql').text)
    }

    List<String> getApps() {
        db.rows('''
SELECT DISTINCT app_id FROM et_scene
WHERE deleted_at IN ('1970-01-01 08:00:00', '0000-00-00 00:00:00')
''').collect { it.app_id as String }

    }

    List<SceneDO> getScenesByApp(String appId) {
        if (appId == null || appId.isAllWhitespace()) return []

        def rows = db.rows("""
SELECT * FROM et_scene WHERE app_id = $appId
AND deleted_at IN ('1970-01-01 08:00:00', '0000-00-00 00:00:00')
ORDER BY id
-- ORDER BY scene_status DESC, created_at
""")
        toBeans(rows, SceneDO)
    }

    SceneDO getSceneByAppCode(String appId, String sceneCode) {
        if (appId == null || appId.isAllWhitespace()
        || sceneCode == null || sceneCode.isAllWhitespace()) return null

        def row = db.firstRow("""
SELECT * FROM et_scene WHERE 
app_id = $appId and scene_code = $sceneCode
AND deleted_at IN ('1970-01-01 08:00:00', '0000-00-00 00:00:00')
LIMIT 1
""")
        toBean(row, SceneDO)
    }

    SceneRuleDO getRuleById(Long id) {
        if (id == null) return null

        def row = db.firstRow("""
SELECT * FROM et_scene_rule WHERE id = $id  
AND deleted_at IN ('1970-01-01 08:00:00', '0000-00-00 00:00:00')
LIMIT 1
""")
        toBean(row, SceneRuleDO)
    }

    List<SceneRuleDO> getRulesByApp(String appId) {
        if (appId == null || appId.isAllWhitespace()) return []

        def rows = db.rows("""
SELECT * FROM et_scene_rule WHERE app_id = $appId 
AND deleted_at IN ('1970-01-01 08:00:00', '0000-00-00 00:00:00')
ORDER BY priority
""")
        toBeans(rows, SceneRuleDO)
    }

    List<SceneRuleDO> getRulesByAppCode(String appId, String sceneCode) {
        if (appId == null || appId.isAllWhitespace()
                || sceneCode == null || sceneCode.isAllWhitespace()) return []

        def rows = db.rows("""
SELECT * FROM et_scene_rule WHERE app_id = $appId AND scene_code = $sceneCode 
AND deleted_at IN ('1970-01-01 08:00:00', '0000-00-00 00:00:00')
ORDER BY priority 
""")
        toBeans(rows, SceneRuleDO)
    }

    List<SceneActionDO> getActionsByCodes(String appId, List<String> codes) {
        if (appId == null || appId.isAllWhitespace()) return []
        codes = codes?.findAll{ it != null & !it.isAllWhitespace()}
        if (!codes) return []

        def rows = db.rows("""
SELECT * FROM et_scene_action WHERE action_code IN (${codes.collect{'?'}.join(',')})
AND app_id = ?
AND deleted_at IN ('1970-01-01 08:00:00', '0000-00-00 00:00:00')
""", (codes + [appId]) as List<Object>)
        toBeans(rows, SceneActionDO)
    }

    List<SceneActionDO> getActionsByScene(String appId, String sceneCodes) {
        if (appId == null || appId.isAllWhitespace()
                || sceneCodes == null || sceneCodes.isAllWhitespace()) return []

        def rows = db.rows("""
SELECT * FROM et_scene_action WHERE scene_code = $sceneCodes AND app_id = $appId
AND deleted_at IN ('1970-01-01 08:00:00', '0000-00-00 00:00:00')
""")
        toBeans(rows, SceneActionDO)
    }

    List<SceneRuleExprDO> getExprsByIds(List<Long> ids) {
        ids = ids?.findAll{ it}
        if (!ids) return []

        def rows = db.rows("""
SELECT * FROM et_scene_rule_expr WHERE id IN (${ids.collect {'?'}.join(',')})
AND deleted_at IN ('1970-01-01 08:00:00', '0000-00-00 00:00:00')
""", ids as List<Object>)
        toBeans(rows, SceneRuleExprDO)
    }

    List<SceneRuleExprDO> getExprsByAppCode(String appId, String sceneCode) {
        if (appId == null || appId.isAllWhitespace()
                || sceneCode == null || sceneCode.isAllWhitespace()) return []

        def rows = db.rows("""
SELECT * FROM et_scene_rule_expr WHERE app_id = $appId AND scene_code = $sceneCode
AND deleted_at IN ('1970-01-01 08:00:00', '0000-00-00 00:00:00')
""")
        toBeans(rows, SceneRuleExprDO)
    }

    List<SceneVarDO> getRuleVarsByIds(List<Long> ids) {
        ids = ids?.findAll{ it}
        if (!ids) return []

        def rows = db.rows("""
SELECT * FROM et_scene_var WHERE id IN (${ids.collect{'?'}.join(',')})
AND deleted_at IN ('1970-01-01 08:00:00', '0000-00-00 00:00:00')
""", ids as List<Object>)
        toBeans(rows, SceneVarDO)
    }

    List<SceneVarDO> getVarsByAppCode(String appId, String sceneCode) {
        if (appId == null || appId.isAllWhitespace()
                || sceneCode == null || sceneCode.isAllWhitespace()) return []

        def rows = db.rows("""
SELECT * FROM et_scene_var WHERE app_id = $appId AND scene_code = $sceneCode
AND deleted_at IN ('1970-01-01 08:00:00', '0000-00-00 00:00:00')
""")
        toBeans(rows, SceneVarDO)
    }

    int insertRule(SceneRuleDO rule) {
        if (rule == null
                || rule.appId == null || rule.appId.isAllWhitespace()
                || rule.sceneId == null || rule.sceneCode == null || rule.sceneCode.isAllWhitespace()
                || rule.rule == null
                || rule.ruleType == null
                || rule.ruleName == null || rule.ruleName.isAllWhitespace()
                || rule.ruleDesc == null || rule.ruleDesc.isAllWhitespace()
                || rule.priority == null || rule.actionsCode == null) return -1

        db.executeInsert("""
INSERT INTO et_scene_rule 
(app_id, scene_id, scene_code, rule, rule_type, rule_code, rule_name, rule_desc, priority, actions_code) VALUES (
?.appId, ?.sceneId, ?.sceneCode, ?.rule, ?.ruleType, ?.ruleCode, ?.ruleName, ?.ruleDesc, ?.priority, ?.actionsCode
)
""", rule)
        db.updateCount
    }

    int updateRule(SceneRuleDO rule) {
        if (rule == null
                || rule.rule == null
                || rule.ruleType == null
                || rule.ruleName == null || rule.ruleName.isAllWhitespace()
                || rule.ruleDesc == null || rule.ruleDesc.isAllWhitespace()
                || rule.priority == null || rule.actionsCode == null) return -1

        db.execute("""
update et_scene_rule 
set rule = ${rule.rule},
rule_type = ${rule.ruleType},
rule_name = ${rule.ruleName},
rule_desc = ${rule.ruleDesc},
priority = ${rule.priority},
actions_code = ${rule.actionsCode} 
where id = ${rule.id}
LIMIT 1
""")
        db.updateCount
    }

    int insertScene(SceneDO scene) {
        if (scene == null
                || scene.appId == null
                || scene.appId.isAllWhitespace()
                || scene.sceneName == null
                || scene.sceneName.isAllWhitespace()
                || scene.sceneCode == null
                || scene.sceneCode.isAllWhitespace()
                || scene.sceneDesc == null
                || scene.sceneDesc.isAllWhitespace()
                || scene.sceneType == null) return -1

        db.execute("""
INSERT INTO et_scene (app_id, scene_name, scene_code, scene_desc, scene_type) VALUES ( 
  ?.appId, ?.sceneName, ?.sceneCode, ?.sceneDesc, ?.sceneType 
)
""", scene)
        db.updateCount
    }

    boolean switchScene(String appName, String sceneCode, boolean flag) {
        if (sceneCode == null || sceneCode.isAllWhitespace()) return false
        Byte status = flag ? SceneDO.enabled : SceneDO.disabled

        if (appName == null || appName.isAllWhitespace()
                || sceneCode == null || sceneCode.isAllWhitespace() || status == null)
            return -1

        db.execute("""
update et_scene set  
scene_status = $status
where scene_code = $sceneCode and app_id = $appName
limit 1
""")
        db.updateCount
    }

    // 只能更新 name/desc/type 必须同时更新
    int updateScene(SceneDO scene) {
        if (!scene || !scene.id
                || scene.sceneName == null || scene.sceneName.isAllWhitespace()
                || scene.sceneDesc == null || scene.sceneDesc.isAllWhitespace()
                || scene.sceneType == null
        ) return -1

        db.execute("""
update et_scene set  
scene_name = ${scene.sceneName},
scene_desc = ${scene.sceneDesc},
scene_type = ${scene.sceneType}
where id = ${scene.id}
limit 1
""")
        db.updateCount
    }

    int deleteVar(Long id) {
        // TODO disabled
    }

    int deleteScene(Long id) {
        // TODO disabled
    }

    int insertVar(SceneVarDO var) {
        // TODO 检查 sceneId 与 sceneCode 是否存在 !!!
        if (var == null
                || var.appId == null || var.appId.isAllWhitespace()
                || var.sceneId == null
                || var.sceneCode == null || var.sceneCode.isAllWhitespace()
                || var.varName == null || var.varName.isAllWhitespace()
                || var.varType == null || var.varType.isAllWhitespace()
                || var.varDesc == null || var.varDesc.isAllWhitespace()
        ) return -1

        db.execute("""
INSERT INTO et_scene_var (app_id, scene_id, scene_code, var_name, var_type, var_desc) VALUES (
  ?.appId, ?.sceneId, ?.sceneCode, ?.varName, ?.varType, ?.varDesc
)
""", var)
        db.updateCount
    }

    // 只能更新 name/desc 必须同时更新
    int updateVar(SceneVarDO var) {
        if (!var || !var.id
                || var.varName == null || var.varName.isAllWhitespace()
                || var.varType == null || var.varType.isAllWhitespace()
                || var.varDesc == null || var.varDesc.isAllWhitespace()) return -1

        db.execute("""
update et_scene_var set 
var_name = ${var.varName},
var_type = ${var.varType}, 
var_desc = ${var.varDesc} 
where id = ${var.id}
limit 1
""")
        db.updateCount
    }

    int insertAction(SceneActionDO action) {
        // TODO 检查 sceneId 与 sceneCode 是否存在 !!!
        if (action == null
                || action.appId == null
                || action.appId.isAllWhitespace()
                || action.sceneId == null
                || action.sceneCode == null
                || action.sceneCode.isAllWhitespace()
                || action.actionCode == null
                || action.actionCode.isAllWhitespace()
                || action.actionDesc == null
                || action.actionDesc.isAllWhitespace()
                || action.action == null
                || action.action.isAllWhitespace()
        ) return -1

        db.execute("""
INSERT INTO et_scene_action (
app_id, scene_id, scene_code, action_code, action_desc, action) VALUES (
?.appId, ?.sceneId, ?.sceneCode, ?.actionCode, ?.actionDesc, ?.action
)
""", action)
        db.updateCount
    }

    // 只能更新 desc 和 内容, 不能更新编号
    int updateAction(SceneActionDO action) {
        if (!action || !action.id
                || action.actionCode == null || action.actionCode.isAllWhitespace()
                || action.actionDesc == null || action.actionDesc.isAllWhitespace()
                || action.action == null || action.action.isAllWhitespace()) return -1

        db.execute("""
update et_scene_action 
set action_desc = ${action.actionDesc},
action = ${action.action}
where id = ${action.id}
limit 1
""")
        db.updateCount
    }

    int insertExpr(SceneRuleExprDO expr) {
        // TODO 检查 sceneId 与 sceneCode 是否存在 !!!
        if (expr == null
                || expr.appId == null || expr.appId.isAllWhitespace()
                || expr.sceneId == null
                || expr.sceneCode == null || expr.sceneCode.isAllWhitespace()
                || expr.exprVar == null
                || expr.exprOp == null || expr.exprOp.isAllWhitespace()
                || expr.exprVal == null || expr.exprVal.isAllWhitespace()
        ) return -1

        db.execute("""
INSERT INTO et_scene_rule_expr (app_id, scene_id, scene_code, expr_var, expr_op, expr_val) VALUES (
  ?.appId, ?.sceneId, ?.sceneCode, ?.exprVar, ?.exprOp, ?.exprVal
)
""", expr)
        db.updateCount
    }

    int updateExpr(SceneRuleExprDO expr) {
        if (!expr || !expr.id
                || expr.exprVar == null
                || expr.exprOp == null || expr.exprOp.isAllWhitespace()
                || expr.exprVal == null || expr.exprVal.isAllWhitespace()) return -1

        db.execute("""
update et_scene_rule_expr set 
expr_var = ${expr.exprVar},
expr_op = ${expr.exprOp}, 
expr_val = ${expr.exprVal} 
where id = ${expr.id}
limit 1
""")
        db.updateCount
    }

    static List<String> parserActionCodes (String codes) {
        if (codes == null || codes.isAllWhitespace()) return []
        if (codes.indexOf(',') == -1) {
            [codes] as List<String>
        } else {
            codes.tokenize(',')
                    .findAll { it != null && !it.isAllWhitespace() }
                    .collect { it.trim() }  as List<String>
        }
    }

    static List<String> getActionsCodesByRule (SceneRuleDO rule) {
        if (!rule) return []
        rule.actionsCode.tokenize(',')
                .findAll { it != null && !it.isAllWhitespace() }
                .collect { it.trim() }  as List<String>
    }

    static List<String> getActionsCodesByRules (List<SceneRuleDO> rules) {
        if (!rules) return []
        rules.collect{
            it.actionsCode.tokenize(',')
                    .findAll { it != null && !it.isAllWhitespace() }
                    .collect { it.trim() }
        }.flatten().unique() as List<String>
    }

    static List<Long> getExprIdsByRules(List<SceneRuleDO> rules) {
        if (!rules) return []
        rules.findAll { it.ruleType == SceneRuleDO.expr }.collect {
            // it.rule.findAll(/\{(\d+)\}/).collect { it[1] as Long }
            it.rule.findAll(/\{\d+\}/).collect { it[1..-2] as Long }
        }.flatten().unique() as List<Long>
    }

    static <T> T toBean(GroovyRowResult row, Class<T> kind) {
        if (row == null) return null
        def bean = kind.newInstance()
        row.each {
            def k = (it.key as String).replaceAll(~/_(\w)/){ List<String> strs ->
                (strs[1] as String).toUpperCase()
            }
            if (bean.hasProperty(k)) {
                bean[k] = it.value
            }
        }
        bean
    }

    static <T> List<T> toBeans(List<GroovyRowResult> rows, Class<T> kind) {
        rows.collect { row -> toBean(row, kind) } as List<T>
    }
}
