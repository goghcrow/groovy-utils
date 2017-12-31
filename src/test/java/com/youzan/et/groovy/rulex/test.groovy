package com.youzan.et.groovy.rulex

import com.youzan.et.groovy.rulex.datasrc.VarType

println VarType.typeList
println VarType.typeMap

println VarType.TLong.sanitize(1)
println VarType.TLong.sanitize("1")
try {
    println VarType.TLong.sanitize("3.14")
}catch (e) { println e }
try {
println VarType.TLong.sanitize("hello")
}catch (e) { println e }

println VarType.TDouble.sanitize(1)
println VarType.TDouble.sanitize(" 1")
println VarType.TDouble.sanitize(3.14)
println VarType.TDouble.sanitize("3.14 ")
try {
    println VarType.TDouble.sanitize("hello")
}catch (e) { println e }

println VarType.TString.sanitize(1)
println VarType.TString.sanitize(3.14)
println VarType.TString.sanitize("hello ")


println VarType.TListLong.sanitize([1,'1'])
println VarType.TListDouble.sanitize([1, '3.14'])
println VarType.TListString.sanitize([1, '3.14'])