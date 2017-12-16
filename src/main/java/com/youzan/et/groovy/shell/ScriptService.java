//package com.youzan.et.groovy;
//
//import lombok.NonNull;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.stereotype.Service;
//
//import javax.annotation.Resource;
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.stream.Collectors;
//
///**
// * @author chuxiaofeng
// */
//@Service
//@Slf4j
//@ConfigurationProperties(prefix = "script")
//public class ScriptService {
//    private static final String BINDING_KEY_LOG = "$log";
//    private static final String BINDING_KEY_ARGS_TYPE = "$argsType";
//    private static final String BINDING_KEY_ARGS = "$args";
//    private static final String BINDING_KEY_DS_EOO = "$dsEoo";
//    private static final String BINDING_KEY_DS_JIRA = "$dsJira";
//    private static final String BINDING_KEY_DS_XL = "$dsXl";
//
//    @Resource
//    private
//    Groovy groovy;
//
//    @Resource(name = "dataSource")
//    private
//    DruidDataSource eooDataSource;
//
//    @Resource(name = "jiraDataSource")
//    private
//    DruidDataSource jiraDataSource;
//
//    @Resource(name = "xlDataSource")
//    private
//    DruidDataSource xlDataSource;
//
//    // "http://gitlab.qima-inc.com/ET/et-script/raw/master/src/main/java/com/youzan/et/eoo/script/";
//    private String scriptUriPrefix;
//
//    private static Map<String, Class<?>> TYPE_CACHE = new ConcurrentHashMap<>();
//
//    private Class<?> getReturnType(@NonNull String typeString) {
//        if (TYPE_CACHE.containsKey(typeString)) {
//            return TYPE_CACHE.get(typeString);
//        }
//
//        try {
//            Class<?> type = Class.forName(typeString);
//            TYPE_CACHE.putIfAbsent(typeString, type);
//            return type;
//        } catch (ClassNotFoundException e) {
//            log.error("获取脚本返回类型失败: " + typeString, e);
//            return Object.class;
//        }
//    }
//
//    private Object makeKind(Class<?> kind) {
//        try {
//            return kind.newInstance();
//        } catch (InstantiationException | IllegalAccessException ignored) { }
//        return new Object();
//    }
//
//    private URI getScriptURIByKey(@NonNull String key) throws URISyntaxException {
//        return new URI(SCRIPT_URI_PREFIX + key + ".groovy");
//    }
//
//    public Object call(@NonNull String key) {
//        return call(key, null);
//    }
//
//    public Object call(@NonNull String key, Object args) {
//        return internalCall(key, args, false);
//    }
//
//    public Object callWithJSONArgs(@NonNull String key, String jsonArgs) {
//        return internalCall(key, jsonArgs, true);
//    }
//
//    public Map<String, ?> call(@NonNull List<String> keys) {
//        return scriptDAO.selectByKeys(keys).stream().parallel()
//                .map(a -> {
//                    Class<?> kind = null;
//                    if (StringUtils.isNotBlank(a.getReturnType())) {
//                        kind = getReturnType(a.getReturnType());
//                    }
//                    try {
//                        Object r = groovy.eval(getScriptURIByKey(a.getScript()), makeBindings(null, null));
//                        return Pair.of(a.getKey(), kind != null ? kind.cast(r) : r);
//                    } catch (Throwable e) {
//                        log.error("脚本执行错误: " + StringUtils.join(keys, ","), e);
//                        return Pair.of(a.getKey(), makeKind(kind));
//                    }
//                })
//                // .peek(System.out::println) // debug
//                .collect(Collectors.toMap(Pair::getCar, Pair::getCdr));
//    }
//
//    private Map<String, Object> makeBindings(Object args, Object argsType) {
//        // 绑定脚本需要的资源
//        Map<String, Object> bindings = new HashMap<>();
//        bindings.put(BINDING_KEY_LOG, log);
//        bindings.put(BINDING_KEY_ARGS_TYPE, argsType);
//        bindings.put(BINDING_KEY_ARGS, args);
//        bindings.put(BINDING_KEY_DS_EOO, eooDataSource);
//        bindings.put(BINDING_KEY_DS_JIRA, jiraDataSource);
//        bindings.put(BINDING_KEY_DS_XL, xlDataSource);
//        return bindings;
//    }
//
//    private Object internalCall(@NonNull String key, Object args, boolean isJSONStr) {
//        List<ScriptDO> list = scriptDAO.selectByKeys(Lists.newArrayList(key));
//        if (list == null || list.isEmpty()) {
//            throw new EooScriptException(Errors. SCRIPT_NOT_FOUND);
//        }
//
//        ScriptDO s = list.get(0);
//        try {
//            if (isJSONStr) {
//                if (StringUtils.isNotBlank(s.getParamType())) {
//                    if (StringUtils.isNotBlank((String) args)) {
//                        Class<?> type = Class.forName(s.getParamType());
//                        args = JSON.parseObject((String) args, type);
//                    }
//                }
//            }
//
//            Object r = groovy.eval(getScriptURIByKey(s.getScript()), makeBindings(args, s.getParamType()));
//            if (StringUtils.isNotBlank(s.getReturnType())) {
//                r = getReturnType(s.getReturnType()).cast(r);
//            }
//            return r;
//        } catch (Exception e) {
//            log.error("脚本执行错误: " + key, e);
//            throw new EooScriptException(Errors.SCRIPT_EVAL_ERROR.getCode(), e.getMessage());
//        }
//    }
//
//    public void cacheClear(@NonNull List<String> keys) {
//        scriptDAO.selectByKeys(keys).stream().parallel()
//                .forEach(a -> {
//                    try { Groovy.clearCache(getScriptURIByKey(a.getScript())); } catch (Throwable ignored) { }
//                });
//    }
//
//    public void cacheClearAll() {
//        Groovy.clearAllCache();
//    }
//}
