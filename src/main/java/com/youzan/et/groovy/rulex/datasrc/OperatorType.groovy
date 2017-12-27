package com.youzan.et.groovy.rulex.datasrc

import groovy.transform.CompileStatic

@CompileStatic
enum OperatorType {
    LT(operator: '<') {
        String compile(String left, String rightType, String rightVal) {
            def right = VarType.typeOf(rightType)?.sanitize(rightVal)
            if (right == null || !VarType.typeOf(rightType)?.scalar) {
                throw new RuntimeException("非法表达式 leftName=$left, rightType=$rightType, rightVal=$rightVal")
            }
            "$left < $right"
        }
    },
    GT(operator: '>') {
        String compile(String left, String rightType, String rightVal) {
            def right = VarType.typeOf(rightType)?.sanitize(rightVal)
            if (right == null || !VarType.typeOf(rightType)?.scalar) {
                throw new RuntimeException("非法表达式 leftName=$left, rightType=$rightType, rightVal=$rightVal")
            }
            "$left > $right"
        }
    },
    LTE(operator: '<=') {
        String compile(String left, String rightType, String rightVal) {
            def right = VarType.typeOf(rightType)?.sanitize(rightVal)
            if (right == null || !VarType.typeOf(rightType)?.scalar) {
                throw new RuntimeException("非法表达式 leftName=$left, rightType=$rightType, rightVal=$rightVal")
            }
            "$left <= $right"
        }
    },
    GTE(operator: '>=') {
        String compile(String left, String rightType, String rightVal) {
            def right = VarType.typeOf(rightType)?.sanitize(rightVal)
            if (right == null || !VarType.typeOf(rightType)?.scalar) {
                throw new RuntimeException("非法表达式 leftName=$left, rightType=$rightType, rightVal=$rightVal")
            }
            "$left >= $right"
        }
    },
    NE(operator: '!=') {
        String compile(String left, String rightType, String rightVal) {
            def right = VarType.typeOf(rightType)?.sanitize(rightVal)
            if (right == null || !VarType.typeOf(rightType)?.scalar) {
                throw new RuntimeException("非法表达式 leftName=$left, rightType=$rightType, rightVal=$rightVal")
            }
            "$left != $right"
        }
    },
    EQ(operator: '==') {
        String compile(String left, String rightType, String rightVal) {
            def right = VarType.typeOf(rightType)?.sanitize(rightVal)
            if (right == null || !VarType.typeOf(rightType)?.scalar) {
                throw new RuntimeException("非法表达式 leftName=$left, rightType=$rightType, rightVal=$rightVal")
            }
            "$left == $right"
        }
    },
    IN(operator: 'IN') {
        String compile(String left, String rightType, String rightVal) {
            def right = VarType.typeOf(rightType)?.sanitize(rightVal)
            if (right == null || VarType.typeOf(rightType)?.scalar) {
                throw new RuntimeException("非法表达式 leftName=$left, rightType=$rightType, rightVal=$rightVal")
            }
            "${right}.contains($left)"
        }
    },
    NIN(operator: 'NOT IN') {
        String compile(String left, String rightType, String rightVal) {
            def right = VarType.typeOf(rightType)?.sanitize(rightVal)
            if (right == null || VarType.typeOf(rightType)?.scalar) {
                throw new RuntimeException("非法表达式 leftName=$left, rightType=$rightType, rightVal=$rightVal")
            }
            "!${right}.contains($left)"
        }
    }

// TODO 后续支持
//    ,AND(op: 'and'),
//    NOT(op: 'not'),
//    OR(op: 'or'),
//
//    EXISTS(op: 'exists'),
//    REGEX(op: 'regex'),
//    TEXT(op: 'text'),
//    ALL(op: 'all'),
//    SIZE(op: 'size')

    String operator
    String operand

    // abstract String compile(String left, String right);
    String compile(String left, String rightType, String rightVal) {}

    final static Map<String, OperatorType> opMap = values().collectEntries { [((it as OperatorType).operator): it as OperatorType]}
    final static List<String> opList = values().collect { (it as OperatorType).operator }
    static OperatorType ofOp(String operator) { opMap[operator] }
}