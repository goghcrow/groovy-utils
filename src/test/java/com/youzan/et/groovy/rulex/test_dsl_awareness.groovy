package com.youzan.et.groovy.rulex

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