package com.youzan.et.groovy.gctest;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

// https://my.oschina.net/chenxiaojie/blog/1587651

@Slf4j
public class GroovyClassLoaderUtils {

    private static final GroovyClassLoader classLoader = new AutoCleanGroovyClassLoader();

    public static Script loadScript(String clazz) {
        return loadScript(clazz, new Binding());
    }

    public static Script loadScript(String clazz, Binding binding) {
        Class _clazz = loadClass(clazz);
        if (_clazz == null) {
            return null;
        }
        return InvokerHelper.createScript(_clazz, binding);
    }

    public static Class loadClass(String clazz) {
        if (StringUtils.isEmpty(clazz)) {
            return null;
        }
        Class _clazz = null;
        try {
            _clazz = classLoader.parseClass(clazz);
            log.info("load class:" + clazz + " success!");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            classLoader.clearCache();
        }
        return _clazz;
    }

    private static class AutoCleanGroovyClassLoader extends GroovyClassLoader {

        private final List<ClassLoader> classLoaders = new ArrayList<>();

        private final Pattern pattern = Pattern.compile("script\\d+");

        public AutoCleanGroovyClassLoader() {
            this(Thread.currentThread().getContextClassLoader());
        }

        public AutoCleanGroovyClassLoader(ClassLoader parent) {

            super(parent);

            if (parent == null) {
                throw new RuntimeException("父类加载器不能为空!");
            }

            classLoaders.add(this);

            while (parent != null) {
                classLoaders.add(parent);
                parent = parent.getParent();
            }

            Field _parallelLockMap = null;
            try {
                _parallelLockMap = ClassLoader.class.getDeclaredField("parallelLockMap");
                _parallelLockMap.setAccessible(true);
            } catch (NoSuchFieldException e) {
            }

            if (_parallelLockMap != null) {
                final Field parallelLockMap = _parallelLockMap;
                new Thread(() -> {
                    while (true) {
                        try {
                            for (ClassLoader classLoader : classLoaders) {
                                Map<String, Object> map = (Map<String, Object>) parallelLockMap.get(classLoader);
                                if (map != null && map.size() > 10000) {
                                    Iterator<Map.Entry<String, Object>> iterator = map.entrySet().iterator();
                                    while (iterator.hasNext()) {
                                        Map.Entry<String, Object> lockEntry = iterator.next();
                                        if (pattern.matcher(lockEntry.getKey()).find()) {
                                            iterator.remove();
                                            log.info("auto clean lock " + lockEntry.getKey());
                                        }
                                    }
                                }
                            }
                            Thread.sleep(60000);
                        } catch (Throwable e) {
                            log.error(e.getMessage(), e);
                        }
                    }
                }).start();
            }
        }
    }
}
