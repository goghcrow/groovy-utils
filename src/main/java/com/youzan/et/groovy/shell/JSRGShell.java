package com.youzan.et.groovy.shell;

import jdk.nashorn.api.scripting.URLReader;
import lombok.SneakyThrows;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.script.*;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;


class JSRGShell implements ApplicationContextAware {
    private ApplicationContext ctx;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ctx = applicationContext;
    }

    // 注意: JSR 方式 暂时不支持 CompilerConfiguration
    private final Map<String, CompiledScript> scriptCache = new ConcurrentHashMap<>();

    private Compilable getCompiler() {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine groovyEngine = manager.getEngineByName("groovy");
        Objects.requireNonNull(groovyEngine);
        /*
        StringWriter sw = new StringWriter();
        PrintWriter writer = new PrintWriter(sw);
        groovyEngine.getContext().setWriter(writer);
        groovyEngine.getContext().setErrorWriter(writer);
        sw.toString();
        */
        groovyEngine.put("ctx", ctx); // NonThreadSafe
        return (Compilable) groovyEngine;
    }

    private CompiledScript cacheCompile(String code) throws IOException, ScriptException {
        Objects.requireNonNull(code);
        CompiledScript compiledScript = scriptCache.get(code);
        if (compiledScript == null) {
            compiledScript = getCompiler().compile(code);
            scriptCache.putIfAbsent(code, compiledScript);
        }
        return compiledScript;
    }

    private CompiledScript cacheCompile(URI uri) throws IOException, ScriptException {
        Objects.requireNonNull(uri);
        URL url = uri.toURL();
        CompiledScript compiledScript = scriptCache.get(url.toExternalForm());
        if (compiledScript == null) {
            compiledScript = getCompiler().compile(new URLReader(uri.toURL(), "UTF-8"));
            scriptCache.putIfAbsent(url.toExternalForm(), compiledScript);
        }
        return compiledScript;
    }

    public Object eval(URI uri) throws IOException, ScriptException {
        return cacheCompile(uri).eval();
    }

    public Object eval(String string) throws IOException, ScriptException {
        return cacheCompile(string).eval();
    }

    public Object eval(URI uri, Map<String, Object> bindings) throws IOException, ScriptException {
        return cacheCompile(uri).eval(new SimpleBindings(bindings));
    }

    public Object eval(String string, Map<String, Object> bindings) throws IOException, ScriptException {
        return cacheCompile(string).eval(new SimpleBindings(bindings));
    }

    public void cacheClear(URL url) {
        scriptCache.remove(url.toExternalForm());
    }

    public void cacheClear() {
        scriptCache.clear();
    }

    @SneakyThrows
    private static String cacheKey(String code) {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(code.getBytes());
        byte[] digest = md.digest();
        return DatatypeConverter.printHexBinary(digest);
    }

    private static String cacheKey(URI uri) {
        return uri.normalize().toString();
    }
}