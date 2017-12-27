package com.youzan.et.groovy.gpars

import groovyx.gpars.dataflow.Dataflows
import static groovyx.gpars.actor.Actors.*
import static groovyx.gpars.GParsPool.withPool
import static groovyx.gpars.dataflow.Dataflow.task


def ping = actor {
    loop {
        react {
            println it
            sleep(500)
            reply 'pong'
        }
    }
}

def pong = actor {
    loop {
        react {
            println it
            sleep(500)
            reply 'ping'
        }
    }
    ping << 'ping'

}

[ping, pong]*.join()