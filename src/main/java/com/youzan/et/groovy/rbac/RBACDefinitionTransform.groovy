package com.youzan.et.groovy.rbac

import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
class RBACDefinitionTransform implements ASTTransformation {
    // TODO
    private static List<String> defList = ['api', 'role', 'user', 'users', 'group']

    private static void visitScriptCode(SourceUnit source, GroovyCodeVisitor transformer) {
        source.getAST().getStatementBlock().visit(transformer)
        for (Object method : source.getAST().getMethods()) {
            MethodNode methodNode = (MethodNode) method
            methodNode.getCode().visit(transformer)
        }
    }

    void visit(ASTNode[] nodes, SourceUnit unit) {
        visitScriptCode(unit, new ClassCodeVisitorSupport() {
            protected SourceUnit getSourceUnit() { unit }
            void visitMethodCallExpression(MethodCallExpression mCall) {
                doVisitMethodCallExpression(mCall)
                super.visitMethodCallExpression(mCall)
            }

            private void doVisitMethodCallExpression(MethodCallExpression call) {
                // println call.text

                if (!defList.any { isInstanceMethod(call, it) }) {
                    return
                }

                ArgumentListExpression args = (ArgumentListExpression) call.getArguments()
                if (args.getExpressions().size() == 0) {
                    return
                }

                // Matches: rbac定义 <arg>{1, n}
                if (args.getExpressions().size() > 1) {
                    for (int i = 0; i < args.getExpressions().size(); i++) {
                        // TODO 这里不能存在变量, 必须都是常量
                        transformVariableExpression(call, i)
                    }
//                    if (args.getExpression(0) instanceof MapExpression && args.getExpression(1) instanceof VariableExpression) {
//                        // Matches: rbac定义 <name-value-pairs>, <identifier>, <arg>?
//                        // Map to: rbac定义(<name-value-pairs>, '<identifier>', <arg>?)
//                        transformVariableExpression(call, 1)
//                    } else if (args.getExpression(0) instanceof VariableExpression) {
//                        // Matches: rbac定义 <identifier>, <arg>?
//                        transformVariableExpression(call, 0)
//                    }
                    return
                }

                // Matches: rbac定义 <arg> or Matches: rbac定义(<arg>)
                Expression arg = args.getExpression(0)
                if (arg instanceof VariableExpression) {
                    // Matches: rbac定义 <identifier> or rbac定义(<identifier>)
                    transformVariableExpression(call, 0)
                } else if (arg instanceof MethodCallExpression) {
                    // Matches: rbac定义 <method-call>
                    maybeTransformNestedMethodCall((MethodCallExpression) arg, call)
                }
            }

            private void transformVariableExpression(MethodCallExpression call, int index) {
                ArgumentListExpression args = (ArgumentListExpression) call.getArguments()
                VariableExpression arg = (VariableExpression) args.getExpression(index)
                if (!isDynamicVar(arg)) {
                    return
                }

                // Matches: rbac定义 args?, <identifier>, args? or rbac定义(args?, <identifier>, args?)
                // Map to: rbac定义(args?, '<identifier>', args?)
                call.setMethod(new ConstantExpression(call.getMethod().getText()))
                args.getExpressions().set(index, new ConstantExpression(arg.getText()))
            }

            private boolean maybeTransformNestedMethodCall(MethodCallExpression nestedMethod, MethodCallExpression target) {
                if (!(isNameDefIdentifier(nestedMethod.getMethod()) && targetIsThis(nestedMethod))) {
                    return false
                }

                // Matches: rbac定义 <identifier> <arg-list> | rbac定义 <string> <arg-list>
                // Map to: rbac定义("<identifier>", <arg-list>) | rbac定义(<string>, <arg-list>)

                Expression mapArg = null
                List<Expression> extraArgs = Collections.emptyList()

                if (nestedMethod.getArguments() instanceof TupleExpression) {
                    TupleExpression nestedArgs = (TupleExpression) nestedMethod.getArguments()
                    if (nestedArgs.getExpressions().size() == 2 && nestedArgs.getExpression(0) instanceof MapExpression && nestedArgs.getExpression(1) instanceof ClosureExpression) {
                        // Matches: rbac定义 <identifier>(<options-map>) <closure>
                        mapArg = nestedArgs.getExpression(0)
                        extraArgs = nestedArgs.getExpressions().subList(1, nestedArgs.getExpressions().size())
                    } else if (nestedArgs.getExpressions().size() == 1 && nestedArgs.getExpression(0) instanceof ClosureExpression) {
                        // Matches: rbac定义 <identifier> <closure>
                        extraArgs = nestedArgs.getExpressions()
                    } else if (nestedArgs.getExpressions().size() == 1 && nestedArgs.getExpression(0) instanceof NamedArgumentListExpression) {
                        // Matches: rbac定义 <identifier>(<options-map>)
                        mapArg = nestedArgs.getExpression(0)
                    } else if (nestedArgs.getExpressions().size() != 0) {
                        return false
                    }
                }

                target.setMethod(new ConstantExpression(target.getMethod().getText()))
                ArgumentListExpression args = (ArgumentListExpression) target.getArguments()
                args.getExpressions().clear()
                if (mapArg != null) {
                    args.addExpression(mapArg)
                }
                args.addExpression(nestedMethod.getMethod())
                for (Expression extraArg : extraArgs) {
                    args.addExpression(extraArg)
                }
                return true
            }

            private static boolean targetIsThis(MethodCallExpression call) {
                Expression target = call.getObjectExpression()
                return target instanceof VariableExpression && target.getText().equals("this")
            }

            private static boolean isMethodOnThis(MethodCallExpression call, String name) {
                boolean hasName = call.getMethod() instanceof ConstantExpression && call.getMethod().getText().equals(name)
                return hasName && targetIsThis(call)
            }

            private boolean isInstanceMethod(MethodCallExpression call, String name) {
                if (!isMethodOnThis(call, name)) {
                    return false
                }

                return call.getArguments() instanceof ArgumentListExpression
            }

            private boolean isNameDefIdentifier(Expression expression) {
                return expression instanceof ConstantExpression || expression instanceof GStringExpression
            }

            private boolean isDynamicVar(Expression expression) {
                if (!(expression instanceof VariableExpression)) {
                    return false
                }
                VariableExpression variableExpression = (VariableExpression) expression
                return variableExpression.getAccessedVariable() instanceof DynamicVariable
            }
        })
    }
}

