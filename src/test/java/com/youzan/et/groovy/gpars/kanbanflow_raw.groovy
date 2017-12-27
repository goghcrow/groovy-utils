package com.youzan.et.groovy.gpars

import groovyx.gpars.dataflow.DataflowQueue
//import groovyx.gpars.dataflow.operator.DataflowPoisson
import static groovyx.gpars.dataflow.Dataflow.operator
import java.util.concurrent.atomic.AtomicInteger

@Singleton
class DataFlowPoisson {}

def upstream    = new DataflowQueue()                   // empty trays travel back upstream to the producer
def downstream  = new DataflowQueue()                   // trays with products travel to the consumer downstream

def prodWiring = [inputs: [upstream], outputs: [downstream], maxForks: 3 ] // maxForks is optional
def consWiring = [inputs: [downstream], outputs: [upstream], maxForks: 3 ] // maxForks is optional

class Tray { int card; def product }
int wip = prodWiring.maxForks + consWiring.maxForks     // work in progress == max # of products in the system
wip.times { upstream << new Tray(card: it) }            // put empty trays in the system along with its kanban card

def product = new AtomicInteger(0)                      // a dummy example product; could be anything
def soMany  = 1000
operator(prodWiring) { tray ->
    def prod = product.andIncrement                     // producer is used concurrently: be careful with shared state
    if (prod > soMany) {                                // we do not want to produce endlessly in this example
        downstream << DataFlowPoisson.instance          // let the consumer finish his work, then stop
        return
    }
    def zero = tray.card ? '' : "\n"                    // new line for tray number zero
    print "$zero[$tray.card:$prod] "                    // visualize production point
    tray.product = prod                                 // put product in tray
    downstream << tray                                  // send tray with product inside to consumer
}

def consumer = null
consumer = operator(consWiring) { tray ->
    if (tray == DataFlowPoisson.instance) {
        consumer.terminate() // ?!
        return
    }
    print " $tray.card:$tray.product  "                 // visualize product consumption and card association
    tray.product == null                                // optionally remove product from tray
    upstream << tray                                    // send empty tray back upstream
}

consumer.join()                                         // wait for the overall example to finish