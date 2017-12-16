## 几种 Groovy 集成方案的简单对比(个人理解):

### 适用场景

1. GroovyShell 灵活性较 ClassLoader 差, 适用于执行单个脚本, 定制 DSL;
2. GroovyScriptEngine, 可通过自定义 URLConnector 与 URL 来制定灵活的资源加载策略, 适用于多个互相关联脚本从多数据源加载执行;
3. JSR 使用简单, 最大缺点是 无法配置 CompilerConfiguration, 无法定制 DSL
4. GroovyClassLoader 最为灵活, 理论上支持全部特性, 使用需要深度定制, 适用于其他方式无法满足的特殊场景;

### 注意点

#### 1. 内存问题

无论哪种方式, 原则都不要使用单例 GroovyClassLoader, 因为 相同String 每次被 Parse 都会生成新的 Class,
频繁执行会导致大量 Class 得不到 GC;
(需要无 Class 实例, 加载当前 Class 的 CL 已经被 GC, Class 才会被 GC)

方案;

1. 每个 script 都 new 一个 GroovyClassLoader 来装载；
2. 对于 parseClass 后生成的 Class 对象进行cache


#### 2. ThreadSafe

Binding 为 NonThreadSafe, 如果 Binding 非只读, 脚本内部可能会修改 Binding 内容,
则 Binding 不应该关联到 Shell 实例, 而应该关联到 Script 实例;


### 综上

决定使用 GroovyShell, 借助 Shell Parse 获取 Class 并缓存,
不是缓存 Script 实例, 而是在脚本初次执行成功后, 缓存 Class, 之后每次从 缓存的 Class 构建新的 Script 实例执行