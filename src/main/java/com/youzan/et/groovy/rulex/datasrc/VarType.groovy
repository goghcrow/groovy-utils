package com.youzan.et.groovy.rulex.datasrc

import groovy.transform.CompileStatic

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable

@CompileStatic
enum VarType {
//    暂时先不提供这么多类型
//    TBool(kind: Boolean.class, name: Boolean.name),
//    TChar(kind: Character.class, name: Character.name),
//    TByte(kind: Byte.class, name: Byte.name),
//    TShort(kind: Short.class, name: Short.name),
//    TInt(kind: Integer.class, name: Integer.name),
//    TLong(kind: Long.class, name: Long.name),
//    TFloat(kind: Float.class, name: Float.name),
//    TDouble(kind: Double.class, name: Double.name),
//
//    TBoolArr(kind: Boolean[].class, name: Boolean.name + '[]'),
//    TCharArr(kind: Character[].class, name: Character.name + '[]'),
//    TByteArr(kind: Byte[].class, name: Byte.name + '[]'),
//    TShortArr(kind: Short[].class, name: Short.name + '[]'),
//    TIntArr(kind: Integer[].class, name: Integer.name + '[]'),
//    TLongArr(kind: Long[].class, name: Long.name + '[]'),
//    TFloatArr(kind: Float[].class, name: Float.name + '[]'),
//    TDoubleArr(kind: Double[].class, name: Double.name + '[]')
    TObject(scalar: true, type: Object) { Object sanitize(Object obj) {
        obj
    } },
    TLong(scalar: true, type: Long) { Object sanitize(Object obj) {
        Objects.requireNonNull(obj)
        Long.parseLong(obj.toString().trim())
    } },
    TDouble(scalar: true, type: Double) { Object sanitize(Object obj) {
        Objects.requireNonNull(obj)
        Double.parseDouble(obj.toString().trim())
    } },
    TString(scalar: true, type: String) { Object sanitize(Object obj) {
        Objects.requireNonNull(obj)
        obj.toString().trim()
    } },
    TListLong(scalar: false, type: new TypeRef<List<Long>>() {}.type) {
        Object sanitize(Object obj) {
            Objects.requireNonNull(obj)
            switch (obj) {
                case List:
                    (obj as List).findAll { it != null }.collect { Long.parseLong(it.toString().trim()) }
                    break
                case String:
                    (obj as String).tokenize(',')
                            .findAll { it != null && !it.isAllWhitespace() }
                            .collect { Long.parseLong(it.toString().trim()) }
                    break
                default:
                    throw new RuntimeException("expected List<Long>")
            }
        }
    },
    TListDouble(scalar: false, type: new TypeRef<List<Double>>() {}.type) {
        Object sanitize(Object obj) {
            Objects.requireNonNull(obj)
            switch (obj) {
                case List:
                    (obj as List).findAll { it != null }.collect { Double.parseDouble(it.toString().trim()) }
                    break
                case String:
                    (obj as String).tokenize(',')
                            .findAll { it != null && !it.isAllWhitespace() }
                            .collect { Double.parseDouble(it.toString().trim()) }
                    break
                default:
                    throw new RuntimeException("expected List<Double>")
            }
        }
    },
    TListString(scalar: false, type: new TypeRef<List<String>>() {}.type) {
        Object sanitize(Object obj) {
            Objects.requireNonNull(obj)
            switch (obj) {
                case List:
                    (obj as List).findAll { it != null }.collect { it.toString() }
                    break
                case String:
                    (obj as String).tokenize(',')
                            .findAll { it != null && !it.isAllWhitespace() }
                            .collect { it.toString() }
                    break
                default:
                    throw new RuntimeException("expected List<String>")
            }
        }
    }

    Type type
    Boolean scalar

    @SuppressWarnings("GrMethodMayBeStatic")
    // abstract Object sanitize(Object obj)
    Object sanitize(Object obj) { obj }

    final static Map<String, VarType> typeMap = values().collectEntries { [((it as VarType).type.typeName): it as VarType]}
    final static List<String> typeList = values().collect { (it as VarType).type.typeName }
    static VarType typeOf(String typeName) { typeMap[typeName] }


    @CompileStatic
    static class TypeRef<T> {
        final Type type

        TypeRef(){
            Type superClass = this.class.getGenericSuperclass()
            this.type = ((ParameterizedType) superClass).actualTypeArguments[0]
        }

        TypeRef(Type... actualTypeArguments_){
            Class<?> thisClass = this.class
            Type superClass = thisClass.genericSuperclass

            def argType = (ParameterizedType) ((ParameterizedType) superClass).actualTypeArguments[0]
            Type rawType_ = argType.rawType
            Type[] argTypes = argType.actualTypeArguments

            int actualIndex = 0
            for (int i = 0; i < argTypes.length; ++i) {
                if (argTypes[i] instanceof TypeVariable) {
                    argTypes[i] = actualTypeArguments_[actualIndex++]
                    if (actualIndex >= actualTypeArguments_.length) {
                        break
                    }
                }
            }

            this.type = new ParameterizedType() {
                final Type[] actualTypeArguments = argTypes
                final Type   ownerType = thisClass
                final Type   rawType = rawType_
            }
        }
    }


    static Class toClass(String className) {
        def cls = CLASS_MAP[(className)]
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

    final static Map<String, Class<?>> CLASS_MAP = [
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
    ]
}