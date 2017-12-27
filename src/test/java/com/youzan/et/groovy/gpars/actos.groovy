package com.youzan.et.groovy.gpars

import static groovyx.gpars.actor.Actors.*

def decrypt = reactor { String msg -> msg.reverse() }
def audit = reactor { String msg -> println msg }
def main = actor {
    decrypt << 'olleh'
    react { String msg ->
        audit << msg
    }
}
main.join()

audit.stop() // loop 必须 先发送 stop msg
audit.join()
decrypt.stop()
decrypt.join()