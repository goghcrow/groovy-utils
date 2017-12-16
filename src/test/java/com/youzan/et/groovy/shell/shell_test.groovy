package com.youzan.et.groovy.shell

import org.springframework.context.support.StaticApplicationContext

def shell = new GShell()
shell.ctx = new StaticApplicationContext()

println shell.eval('''
println 'hello'
'world'
''')

println shell.eval('''
println 'hello'
'world'
''')

println shell.eval('''
1/0
''')

println shell.eval('''
println ctx
''')

// -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=

def shellJ = new GShellJ()
shellJ.ctx = new StaticApplicationContext()

println shellJ.eval('''
println 'hello'
'world'
''')

println shellJ.eval('''
println 'hello'
'world'
''')

println shellJ.eval('''
1/0
''')

println shellJ.eval('''
println ctx
''')

// -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
println ""

println shell.eval('''
id
''', [id: 42])

println shell.eval('''
def x = 1
''')

println shell.eval('''
x
''')

// -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=

def jsrShell = new JSRGShell()
println jsrShell.eval('''
def x = 1
''')
println jsrShell.eval('''
++x
''')




