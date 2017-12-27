package com.youzan.et.groovy.gpars

import groovyx.gpars.dataflow.KanbanFlow
import groovyx.gpars.dataflow.KanbanTray

import static groovyx.gpars.dataflow.ProcessingNode.node

//def producer = node { KanbanTray below -> below << 1 }
//def consumer = node { KanbanTray above -> println above.take() }
//
//new KanbanFlow().with {
//    link producer to consumer
//    start()
//    sleep(1000)
//    stop()
//}

def producer1 = node { KanbanTray below -> below << 1 }
def producer2 = node { KanbanTray below -> below << 2 }
def consumer1 = node { KanbanTray above -> println above.take() }
def consumer2 = node { KanbanTray above -> println above.take() }
