package com.youzan.et.groovy.shell

import groovy.transform.ToString
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.control.customizers.SecureASTCustomizer
import org.codehaus.groovy.control.customizers.SourceAwareCustomizer
import org.codehaus.groovy.runtime.InvokerHelper
import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

class GShell implements ApplicationContextAware {
    ApplicationContext ctx

    @Override
    void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ctx = applicationContext
    }

    private final static CompilerConfiguration conf = new CompilerConfiguration()

    static {
        def astCustomizer = new ASTTransformationCustomizer(ToString)
        def sourceAwareCustomizer = new SourceAwareCustomizer(astCustomizer)
        // 1. 配置 AST 方案
        // e.g. 以 Bean 结尾的对象自动添加 @ToString ASTTransform
        // sourceAwareCustomizer.baseNameValidator = { it.endsWith 'Bean' }
        // 2. 配置自动导入
        def importer = new ImportCustomizer()
        // 3. 配置安全策略
        def secureCustomize = new SecureASTCustomizer()

        conf.addCompilationCustomizers importer
        conf.addCompilationCustomizers sourceAwareCustomizer
        conf.addCompilationCustomizers secureCustomize
        conf.scriptBaseClass = BaseScript.name
    }

    private static class Union {
        final String str;final URI uri
        Union(String str) { this.str = str;this.uri = null }
        Union(URI uri) { this.uri = uri; this.str = null }
    }

    private final Map<String, Class> classCache = new ConcurrentHashMap<>()

    // 查看源码, 这里绑定 out 可以绕过 System.out, 直接作用与 Groovy Script 对象 print 系列方法
    // Binding 非线程安全, 不要往 Shell 实例设置 Binding, 而要绑定到 Script
    // 同一个 shell 执行多个脚本内部如果操作 Binding 数据, 有风险
    // 这里没有直接使用 GroovyClassLoader, 而是借助 Shell Parse 获取 Class 并缓存
    // 注意, 不是缓存 Script 实例, 而是在脚本初次执行成功后, 缓存 Class, 之后每次执行使用新的 Script 实例
    private Object cacheEval(Union uni, OutputStream out, Map bindings)
            throws CompilationFailedException, IOException {
        def ret
        def script
        def ck = cacheKey(uni)

        if (bindings) {
            bindings.putAll([ctx: ctx, out: new PrintStream(out)])
        } else {
            bindings = [ctx: ctx, out: new PrintStream(out)]
        }
        def binding = new Binding(bindings)

        def shell = new GroovyShell(this.class.classLoader, conf)

        def clazz = classCache.get(ck)
        if (clazz) {
            ret = InvokerHelper.createScript(clazz, binding).run()
        } else {
            if (uni.str == null) {
                script = shell.parse(uni.uri)
            } else {
                script = shell.parse(uni.str)
            }
            script.setBinding(binding)
            ret = script.run()
            classCache.putIfAbsent(ck, script.class)
        }

        ret
    }

    EvalResult eval(String code) {
        eval(code, null)
    }

    EvalResult eval(URI uri) {
        eval(uri, null)
    }

    EvalResult eval(String code, Map bindings) {
        Objects.requireNonNull(code)
        def out = new ByteArrayOutputStream()
        try {
            def ret = cacheEval(new Union(code), out, bindings)
            [out: out.toString(), ret: ret] as EvalResult
        } catch (Exception e) {
            [out: out.toString(), ret: e] as EvalResult
        }
    }

    EvalResult eval(URI uri, Map bindings) {
        Objects.requireNonNull(uri)
        def out = new ByteArrayOutputStream()
        try {
            def ret = cacheEval(new Union(uri), out, bindings)
            [out: out.toString(), ret: ret] as EvalResult
        } catch (Exception e) {
            [out: out.toString(), ret: e] as EvalResult
        }
    }

    void cacheClear(URI uri) {
        classCache.remove(cacheKey(new Union(uri)))
    }

    void cacheClear(String code) {
        classCache.remove(cacheKey(new Union(code)))
    }

    void cacheClear() {
        classCache.clear()
    }

    private static String cacheKey(Union uni) {
        if (uni.str == null) {
            uni.uri.normalize().toString()
        } else {
            MessageDigest digest = MessageDigest.getInstance("MD5")
            digest.update(uni.str.bytes)
            new BigInteger(1, digest.digest())
                    .toString(16).padLeft(32, '0')
        }
    }
}

/*
    @Resource
    private Shell shell;

    @RequestMapping(value = "/script/eval", method = RequestMethod.POST)
    RestResult eval(@RequestParam String code) {
        Shell.EvalResult ret = shell.eval(code);
        return $.retTrue(new HashMap<String, String>(){{
            put("out", ret.getOut() == null ? "null" : ret.getOut());
            put("ret", ret.getRet() == null ? "null" : ret.getRet().toString());
        }});
    }

*/