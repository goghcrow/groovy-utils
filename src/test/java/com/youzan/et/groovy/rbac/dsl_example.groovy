package com.youzan.et.groovy.rbac

import groovy.transform.TupleConstructor
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer

// adding properties to number, operator overloading, and binding constant injection. Sometimes, for more concise elements in your DSLs, you’ll need to combine several techniques simultaneously to achieve your goals of readability and expressivity

class Robot {
    def move(Map m, Direction dir) {
        println "robot moved at $dir by $m.by at ${m.at ?: '1 km/h'}"
    }

    def move2(Direction dir) {
        [by: { Distance dist ->
            [at: { Speed speed -> println "robot moved $dir by $dist at $speed" }] }]
    }

    def deploy(Direction dir) {
        // 奇数参数, 立即执行lambda
        [arm: { -> println "deploy $dir arm" }()]
    }

    def buy(n) {
        // 注意 of 充当占位, 未被使用
        [shares: {of ->
            [:].withDefault { ticker -> println "buy $n shares of $ticker"}
            }]
    }
}

enum Direction {
    left, right
}

enum Unit {
    centimeter('cm', 0.01),
    meter     ('m', 1),
    kilometer ('km', 1000)

    String abbreviation
    double multiplier

    Unit(String abbr, double mult) {
        abbreviation = abbr
        multiplier = mult
    }
    String toString() { abbreviation }
}

enum Duration {
    hour
}

@TupleConstructor
class Distance {
    Number amount
    Unit unit
    Speed div(Duration dur) { return new Speed(amount, unit) }
    String toString() { "$amount$unit" }
}

@TupleConstructor
class Speed {
    Number amount
    Unit unit
    String toString() { "$amount $unit/h"}
}

class DistanceCategory {
    static Distance getCentimeters(Number num) { new Distance(num, Unit.centimeter) }
    static Distance getMeters(Number num) { new Distance(num, Unit.meter) }
    static Distance getKilometers(Number num) { new Distance(num, Unit.kilometer) }
    static Distance getCm(Number num) { new Distance(num, Unit.centimeter) }
    static Distance getM(Number num) { new Distance(num, Unit.meter) }
    static Distance getKm(Number num) { new Distance(num, Unit.kilometer) }
}



abstract class RobotBaseScript extends Script {
    // Binding 注入属性, 或者 Script 基类两种方式声明属性
    def prop = 'prop'
    def getValue () { 'value' }

    // 委托 robot 的全部方法
    @Delegate @Lazy Robot robot = this.binding.robot

    // __call 大小写忽略方法调用
    def invokeMethod(String name, args) {
        getBinding().robot."${name.toLowerCase()}"(*args)
    }
}

class RobotBinding extends Binding {
    private Map variables

    RobotBinding(Map vars) {
        variables = [
                *: vars,
                // 注入枚举
                *: HttpMethod.values().collectEntries{[(it.name()): it]}
        ]
    }
    // __get 忽略大小写
    def getVariable(String name) {
        variables[name.toLowerCase()]
    }
}


def binding = new RobotBinding([
        // 注入闭包
        // hello: RobotBaseScript.&hello,
        // 注入实例
        robot: new Robot(),

        *: Direction.values().collectEntries{[(it.name()): it]},
        h: Duration.hour,
])

def importer = new ImportCustomizer()
// importer.addStarImports a.b.c
// Normal imports  importer.addImports
// An aliased import importer.addImport
// A static import importer.addStaticImport
// An aliased static import importer.addStaticImport
// Star imports importer.addStarImports
// Static star imports importer.addStaticStars
def config = new CompilerConfiguration()
config.addCompilationCustomizers importer
config.scriptBaseClass = RobotBaseScript.name

def shell = new GroovyShell(this.class.classLoader, binding, config)
shell.evaluate'''
//    hello()
//    HELLO()
//    hellO()
//    println prop
//    println value
'''




// 修改 metaClass 会入侵全局, 可以通过 use 方式无污染的使用
//Number.metaClass.getMeters = { new Distance(delegate, Unit.meter) }
//Number.metaClass.getCentimeters = { new Distance(delegate, Unit.centimeter) }
//Number.metaClass.getKilometers = { new Distance(delegate, Unit.kilometer) }

use(DistanceCategory) {
    //move(Direction.left).by(new Distance(amount: 1, unit: Unit.meter)).at(new Speed(amount: 2, unit: Unit.kilometer))
    shell.evaluate '''
move right, by: 3.m, at: 4.km/h
move2 left by 3.m at 4.km/h

deploy right
deploy right arm

buy 200 shares of GOOD
buy 200 shares of11111 GOOD
'''
}

