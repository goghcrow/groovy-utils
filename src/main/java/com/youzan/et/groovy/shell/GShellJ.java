package com.youzan.et.groovy.shell;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import groovy.transform.ToString;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.groovy.control.customizers.SecureASTCustomizer;
import org.codehaus.groovy.control.customizers.SourceAwareCustomizer;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.security.MessageDigest;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
class GShellJ implements ApplicationContextAware {
    private ApplicationContext ctx;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ctx = applicationContext;
    }

    private final static CompilerConfiguration conf = new CompilerConfiguration();

    static {
        ASTTransformationCustomizer astCustomizer = new ASTTransformationCustomizer(ToString.class);;
        SourceAwareCustomizer sourceAwareCustomizer = new SourceAwareCustomizer(astCustomizer);
        // 1. 配置 AST 方案
        // e.g. 以 Bean 结尾的对象自动添加 @ToString ASTTransform
        // sourceAwareCustomizer.baseNameValidator = { it.endsWith 'Bean' }
        // 2. 配置自动导入
        ImportCustomizer importer = new ImportCustomizer();
        // 3. 配置安全策略
        SecureASTCustomizer secureCustomize = new SecureASTCustomizer();

        conf.addCompilationCustomizers(importer);
        conf.addCompilationCustomizers(sourceAwareCustomizer);
        conf.addCompilationCustomizers(secureCustomize);
        conf.setScriptBaseClass(BaseScript.class.getName());
    }

    private static class Union {
        final String str; final URI uri;
        Union(String str) { this.str = str;this.uri = null; }
        Union(URI uri) { this.uri = uri; this.str = null; }
    }

    private final Map<String, Class> classCache = new ConcurrentHashMap<>();

    private Object cacheEval(Union uni, OutputStream out, Map<String, Object> bindings)
            throws CompilationFailedException, IOException {
        Object ret;
        Script script;
        String ck = cacheKey(uni);

        Binding binding = new Binding(bindings);
        binding.setVariable("ctx", ctx);
        binding.setVariable("out", new PrintStream(out));

        GroovyShell shell = new GroovyShell(this.getClass().getClassLoader(), conf);

        Class clazz = classCache.get(ck);
        if (clazz != null) {
            ret = InvokerHelper.createScript(clazz, binding).run();
        } else {
            if (uni.str == null) {
                assert uni.uri != null;
                script = shell.parse(uni.uri);
            } else {
                script = shell.parse(uni.str);
            }
            script.setBinding(binding);
            ret = script.run();
            classCache.putIfAbsent(ck, script.getClass());
        }
        return ret;
    }

    EvalResult eval(String code) {
        return eval(code, null);
    }

    EvalResult eval(URI uri) {
        return eval(uri, null);
    }

    EvalResult eval(String code, Map<String, Object> bindings) {
        Objects.requireNonNull(code);
        OutputStream out = new ByteArrayOutputStream();
        try {
            Object ret = cacheEval(new Union(code), out, bindings);
            return new EvalResult(out.toString(), ret);
        } catch (Exception e) {
            return new EvalResult(out.toString(), e);
        }
    }

    EvalResult eval(URI uri, Map<String, Object> bindings) {
        Objects.requireNonNull(uri);
        OutputStream out = new ByteArrayOutputStream();
        try {
            Object ret = cacheEval(new Union(uri), out, bindings);
            return new EvalResult(out.toString(), ret);
        } catch (Exception e) {
            return new EvalResult(out.toString(), e);
        }
    }

    void cacheClear(URI uri) {
        classCache.remove(cacheKey(new Union(uri)));
    }

    void cacheClear(String code) {
        classCache.remove(cacheKey(new Union(code)));
    }

    void cacheClear() {
        classCache.clear();
    }

    @SneakyThrows
    private static String cacheKey(Union uni) {
        if (uni.str == null) {
            assert uni.uri != null;
            return uni.uri.normalize().toString();
        } else {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(uni.str.getBytes());
            byte[] digest = md.digest();
            return DatatypeConverter.printHexBinary(digest);
        }
    }
}