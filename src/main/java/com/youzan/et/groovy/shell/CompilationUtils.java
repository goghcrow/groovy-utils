package com.youzan.et.groovy.shell;

import groovy.transform.TimedInterrupt;
import groovy.util.logging.Slf4j;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;
import org.codehaus.groovy.syntax.SyntaxException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author chuxiaofeng
 */
public class CompilationUtils {
    // 注意: 仅作为 提醒使用, 只能防止 System.exit() 方式调用
    public final static CompilationCustomizer FORBIDDEN_SYSTEM_EXIT = new CompilationCustomizer(CompilePhase.CANONICALIZATION) {
        @Override
        public void call(SourceUnit source, GeneratorContext context, ClassNode classNode) throws CompilationFailedException {
            new ClassCodeVisitorSupport() {
                @Override
                public void visitMethodCallExpression(MethodCallExpression call) {
                    if ("java.lang.System".equals(call.getObjectExpression().getText())
                            && "exit".equals(call.getMethod().getText())) {
                        source.addError(new SyntaxException(
                                "System.exit() forbidden",
                                call.getLastLineNumber(),
                                call.getColumnNumber()));
                    }
                    super.visitMethodCallExpression(call);
                }

                @Override
                protected SourceUnit getSourceUnit() {
                    return source;
                }
            }.visitClass(classNode);
        }
    };

    public final static CompilationCustomizer SLF4J = new ASTTransformationCustomizer(Slf4j.class);

    public static CompilationCustomizer timedInterrupt(long sec) {
        Map<String, Object> map = new HashMap<>();
        map.put("value", sec);
//        map.put("unit", TimeUnit.MILLISECONDS);
        return new ASTTransformationCustomizer(map, TimedInterrupt.class);
    }

//    public static CompilationCustomizer timedInterrupt(long millis, TimeUnit unit) {
//        Objects.requireNonNull(unit);
//        Map<String, Object> map = new HashMap<>();
//        map.put("value", millis);
//        map.put("unit", unit);
//        return new ASTTransformationCustomizer(map, TimedInterrupt.class);
//    }
}
