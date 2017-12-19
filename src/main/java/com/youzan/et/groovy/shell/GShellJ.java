//package com.youzan.et.groovy.shell;
//
//import groovy.lang.Binding;
//import groovy.lang.GroovyShell;
//import groovy.lang.Script;
//import lombok.SneakyThrows;
//import lombok.extern.slf4j.Slf4j;
//import org.codehaus.groovy.control.CompilationFailedException;
//import org.codehaus.groovy.control.CompilerConfiguration;
//import org.codehaus.groovy.runtime.InvokerHelper;
//import org.springframework.beans.BeansException;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.ApplicationContextAware;
//
//import javax.xml.bind.DatatypeConverter;
//import java.io.IOException;
//import java.io.OutputStream;
//import java.io.PrintStream;
//import java.net.URI;
//import java.security.MessageDigest;
//import java.util.Map;
//import java.util.Objects;
//import java.util.concurrent.ConcurrentHashMap;
//
//@Slf4j
//class GShellJ implements ApplicationContextAware {
//    private final static Long TIMED_INTERRUPT = 2L;
//
//    private ApplicationContext ctx;
//
//    @Override
//    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//        ctx = applicationContext;
//    }
//
//    public final CompilerConfiguration conf = new CompilerConfiguration();
//
//    {
//        conf.addCompilationCustomizers(CompilationUtils.FORBIDDEN_SYSTEM_EXIT);
//        conf.addCompilationCustomizers(CompilationUtils.SLF4J);
//        conf.addCompilationCustomizers(CompilationUtils.timedInterrupt(TIMED_INTERRUPT));
//        conf.setScriptBaseClass(BaseScript.class.getName());
//    }
//
//    private static class Union {
//        final String str; final URI uri;
//        Union(String str) { this.str = str;this.uri = null; }
//        Union(URI uri) { this.uri = uri; this.str = null; }
//    }
//
//    private final Map<String, Class> classCache = new ConcurrentHashMap<>();
//
//    private Object cacheEval(Union uni, Map<String, Object> bindings, OutputStream out)
//            throws CompilationFailedException, IOException {
//        Object ret;
//        Script script;
//        String ck = cacheKey(uni);
//
//        Binding binding = new Binding(bindings);
//        binding.setVariable("ctx", ctx);
//        if (out != null) {
//            binding.setVariable("out", new PrintStream(out));
//        }
//
//        GroovyShell shell = new GroovyShell(this.getClass().getClassLoader(), conf);
//
//        Class clazz = classCache.get(ck);
//        if (clazz != null) {
//            ret = InvokerHelper.createScript(clazz, binding).run();
//        } else {
//            if (uni.str == null) {
//                assert uni.uri != null;
//                script = shell.parse(uni.uri);
//            } else {
//                script = shell.parse(uni.str);
//            }
//            script.setBinding(binding);
//            ret = script.run();
//            classCache.putIfAbsent(ck, script.getClass());
//        }
//        return ret;
//    }
//
//    EvalResult eval(String code) {
//        return eval(code, null);
//    }
//
//    EvalResult eval(URI uri) {
//        return eval(uri, null);
//    }
//
//    EvalResult eval(String code, Map<String, Object> bindings) {
//        Objects.requireNonNull(code);
//        try {
//            Object ret = cacheEval(new Union(code), bindings, null);
//            return new EvalResult(null, ret);
//        } catch (Exception e) {
//            return new EvalResult(null, e);
//        }
//    }
//
//    EvalResult eval(URI uri, Map<String, Object> bindings) {
//        Objects.requireNonNull(uri);
//        try {
//            Object ret = cacheEval(new Union(uri), bindings, null);
//            return new EvalResult(null, ret);
//        } catch (Exception e) {
//            return new EvalResult(null, e);
//        }
//    }
//
//    EvalResult eval(String code, Map<String, Object> bindings, OutputStream out) {
//        Objects.requireNonNull(code);
//        try {
//            Object ret = cacheEval(new Union(code), bindings, out);
//            return new EvalResult(out.toString(), ret);
//        } catch (Exception e) {
//            return new EvalResult(out.toString(), e);
//        }
//    }
//
//    EvalResult eval(URI uri, Map<String, Object> bindings, OutputStream out) {
//        Objects.requireNonNull(uri);
//        try {
//            Object ret = cacheEval(new Union(uri), bindings, out);
//            return new EvalResult(out.toString(), ret);
//        } catch (Exception e) {
//            return new EvalResult(out.toString(), e);
//        }
//    }
//
//    void cacheClear(URI uri) {
//        classCache.remove(cacheKey(new Union(uri)));
//    }
//
//    void cacheClear(String code) {
//        classCache.remove(cacheKey(new Union(code)));
//    }
//
//    void cacheClear() {
//        classCache.clear();
//    }
//
//    @SneakyThrows
//    private static String cacheKey(Union uni) {
//        if (uni.str == null) {
//            assert uni.uri != null;
//            return uni.uri.normalize().toString();
//        } else {
//            MessageDigest md = MessageDigest.getInstance("MD5");
//            md.update(uni.str.getBytes());
//            byte[] digest = md.digest();
//            return DatatypeConverter.printHexBinary(digest);
//        }
//    }
//}