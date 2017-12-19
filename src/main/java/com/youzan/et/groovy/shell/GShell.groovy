package com.youzan.et.groovy.shell

import groovy.transform.CompileStatic
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

@CompileStatic
class GShell implements ApplicationContextAware {
    final static Long TIMED_INTERRUPT = 2L

    static ApplicationContext ctx

    @Override
    void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ctx = applicationContext
    }

    public final CompilerConfiguration conf = new CompilerConfiguration();

    {
        def astCustomizer = new ASTTransformationCustomizer(ToString)
        def sourceAware = new SourceAwareCustomizer(astCustomizer)
        // 1. 配置 AST 方案
        // e.g. 以 Bean 结尾的对象自动添加 @ToString ASTTransform
        // sourceAwareCustomizer.baseNameValidator = { it.endsWith 'Bean' }
        def importer = new ImportCustomizer()          // TODO 配置自动导入
        def secure = new SecureASTCustomizer()         // TODO 配置安全策略, 黑白名单
        secure.indirectImportCheckEnabled = true

        conf.addCompilationCustomizers CompilationUtils.FORBIDDEN_SYSTEM_EXIT
        conf.addCompilationCustomizers CompilationUtils.SLF4J
//        conf.addCompilationCustomizers CompilationUtils.timedInterrupt(TIMED_INTERRUPT)
        conf.addCompilationCustomizers importer
        conf.addCompilationCustomizers sourceAware
        conf.addCompilationCustomizers secure
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
    private Object cacheEval(Union uni, Map bindings, OutputStream out)
            throws CompilationFailedException, IOException {
        def ret
        def script
        def ck = cacheKey(uni)

        if (bindings) {
            bindings.putAll([ctx: ctx]) // 这里也可以PrintWriter (字节流/字符流)
        } else {
            bindings = [ctx: ctx]
        }
        if (out) {
            bindings << [out: new PrintStream(out)]
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
        assert code
        try {
            def ret = cacheEval(new Union(code), bindings, null)
            [ret: ret, out: null] as EvalResult
        } catch (Exception e) {
            [ret: e, out: null] as EvalResult
        }
    }

    EvalResult eval(URI uri, Map bindings) {
        assert uri
        try {
            def ret = cacheEval(new Union(uri), bindings, null)
            [ret: ret, out: null] as EvalResult
        } catch (Exception e) {
            [ret: e, out: null] as EvalResult
        }
    }

    EvalResult eval(String code, Map bindings, OutputStream out) {
        assert code
        try {
            def ret = cacheEval(new Union(code), bindings, out)
            [out: out.toString(), ret: ret] as EvalResult
        } catch (Exception e) {
            [out: out.toString(), ret: e] as EvalResult
        }
    }

    EvalResult eval(URI uri, Map bindings, OutputStream out) {
        assert uri
        try {
            def ret = cacheEval(new Union(uri), bindings, out)
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