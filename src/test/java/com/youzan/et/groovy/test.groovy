package com.youzan.et.groovy

import com.youzan.et.groovy.shell.GShell
import groovy.transform.TimedInterrupt

import java.util.concurrent.TimeUnit


def shell = new GShell()
println shell.eval('''
java.lang.System.exit(1)
''')