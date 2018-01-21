package com.youzan.et.groovy.rulex


// 测试 IDE DSL 预发提示 rulex.gdsl

scene {
    name 'chuxiaofeng'
    rule {
        name ''
        code ''
        desc ''
        order 1
        when {
            make 'xxxBean'
            log.error('xx')
            ctx.getBean('')
            true
        }
        then {

        }
    }
}

rule {
    when {
        true
    }
    then {}
}