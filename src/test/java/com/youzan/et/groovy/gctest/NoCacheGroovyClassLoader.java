package com.youzan.et.groovy.gctest;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.springframework.util.StringUtils;

@Slf4j
public class NoCacheGroovyClassLoader extends GroovyClassLoader {
    private static final GroovyClassLoader classLoader = new GroovyClassLoader();

    public static Script loadScript(String rule) {

        return loadScript(rule, new Binding());

    }

    public static Script loadScript(String scriptCode, Binding binding) {
        Script script = null;
        if (StringUtils.isEmpty(scriptCode)) {
            return null;

        }
        try {
            Class ruleClazz = classLoader.parseClass(scriptCode);
            if (ruleClazz != null) {
                log.info("load script:" + scriptCode + " success!");
                return InvokerHelper.createScript(ruleClazz, binding);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            classLoader.clearCache();
        }
        return script;
    }
}
