package com.youzan.et.groovy.rulex.datasrc

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.transform.CompileStatic

import javax.sql.DataSource

//@CompileStatic
class SceneDAO {

    // static { GroovyRowResult.metaClass.toBean = this.&toBean }

    DataSource ds

    private Sql db() {
        // new Sql(ds)
        Sql.newInstance('jdbc:mysql://127.0.0.1:3306/et_engine?useServerPrepStmts=false&zeroDateTimeBehavior=convertToNull&characterEncoding=utf8',
                'root', '123456', 'com.mysql.jdbc.Driver'
        )
    }

    def getScenesByApp(String appId) {
        assert appId != null
        def rows = db().rows("""
SELECT * FROM et_scene WHERE app_id = $appId 
""")
        toBeans(rows, SceneDO)
    }

    def getRulesByApp(String appId) {
        assert appId != null
        def rows = db().rows("""
SELECT * FROM et_scene_rule WHERE app_id = $appId 
""")
        toBeans(rows, SceneRuleDO)
    }

    static <T> T toBean(GroovyRowResult row, Class<T> kind) {
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
