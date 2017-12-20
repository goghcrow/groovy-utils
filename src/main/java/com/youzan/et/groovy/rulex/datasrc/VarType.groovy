package com.youzan.et.groovy.rulex.datasrc

import groovy.transform.CompileStatic

@CompileStatic
enum VarType {
    TBool(kind: Boolean.class, name: Boolean.name),
    TChar(kind: Character.class, name: Character.name),
    TByte(kind: Byte.class, name: Byte.name),
    TShort(kind: Short.class, name: Short.name),
    TInt(kind: Integer.class, name: Integer.name),
    TLong(kind: Long.class, name: Long.name),
    TFloat(kind: Float.class, name: Float.name),
    TDouble(kind: Double.class, name: Double.name),

    TBoolArr(kind: Boolean[].class, name: Boolean.name + '[]'),
    TCharArr(kind: Character[].class, name: Character.name + '[]'),
    TByteArr(kind: Byte[].class, name: Byte.name + '[]'),
    TShortArr(kind: Short[].class, name: Short.name + '[]'),
    TIntArr(kind: Integer[].class, name: Integer.name + '[]'),
    TLongArr(kind: Long[].class, name: Long.name + '[]'),
    TFloatArr(kind: Float[].class, name: Float.name + '[]'),
    TDoubleArr(kind: Double[].class, name: Double.name + '[]')

    Class<?> kind
    String name

    final static List<String> nameList = values().collect { (it as VarType).name }

    static Class toClass(String className) {
        def cls = [
                'boolean': boolean.class,
                'byte': byte.class,
                'char': char.class,
                'short': short.class,
                'int': int.class,
                'long': long.class,
                'float': float.class,
                'double': double.class,
                'boolean[]': boolean[].class,
                'byte[]': byte[].class,
                'char[]': char[].class,
                'short[]': short[].class,
                'int[]': int[].class,
                'long[]': long[].class,
                'float[]': float[].class,
                'double[]': double[].class
        ][(className)]
        if (cls) return cls

        def classLoader = Thread.currentThread().contextClassLoader
        try {
            String nName = className
            if (className.size() >= 2 && className[-2..-1] == '[]') {
                nName = "[L${className[0..-3]};"
            }
            Class.forName(nName, true, classLoader)
        } catch (ClassNotFoundException e) {
            if (className.indexOf('.') == -1) {
                try {
                    className = "java.lang.$className"
                    if (className[-2..-1] == '[]') {
                        className = "[L${className[0..-3]};"
                    }
                    Class.forName(className, true, classLoader)
                } catch (ClassNotFoundException ignored) {}
            }
            throw e
        }
    }
}