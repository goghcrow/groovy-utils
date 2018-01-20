package com.youzan.et.groovy.rbac

import com.youzan.et.base.api.UserService
import com.youzan.et.base.api.model.UserModel
import com.youzan.et.groovy.rbac.Api.HttpMethod
import com.youzan.et.groovy.rbac.User.UserScope
import groovy.util.logging.Slf4j
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Service
import org.springframework.util.AntPathMatcher
import org.springframework.util.PathMatcher

import javax.annotation.PostConstruct
import javax.annotation.Resource
import javax.servlet.http.HttpServletRequest

/**
 * 配置
 * e.g.
 * rbac.uri=http://gitlab.qima-inc.com/ET/et-script/raw/master/src/main/java/com/youzan/et/xiaolv/rbac.groovy
 * <dubbo:reference id="userService" interface="com.youzan.et.base.api.UserService" protocol="dubbo" registry="haunt" check="false" timeout="1500" />
 */
@Service
@Slf4j
@ConfigurationProperties(prefix = "rbac")
class RBACService {
    private final static String DEFAULT_ROLE_KEY = 'missing'
    private final static int MAX_CACHE_SZ = 10000

    private boolean init = false

    private String uri

    private RBAC rbac

    private Map<String, UserModel> allUsers = [:]

    private Map<String, Set<Api>> userRoles = [:]
    private Set<Api> defaultRole = new HashSet<>()

    private final static PathMatcher pathMatcher = new AntPathMatcher()
    // 注意: 内部的 LRU Cache 使用 Collections.synchronizedMap 实现, 而不是 ConcurrentHashMap !!!
    private static Closure<Boolean> matcher = createCachedMatcher()

    @Resource
    private UserService userService

    // 注意, 这里必须是静态类
    static abstract class RBACBaseScript extends Script {
        @Delegate @Lazy RBAC rbac = this.binding.rbac

        def invokeMethod(String name, args) {
            getBinding().rbac."${name.toLowerCase()}"(*args)
        }
    }

    static class RBACBinding extends Binding {
        private Map variables

        RBACBinding(Map vars) {
            this.variables = [
                    *: vars,
                    *: HttpMethod.values().collectEntries{[(it.name()): it]},
                    *: UserScope.values().collectEntries{[(it.name()): it]}
            ]
        }

        def getVariable(String name) {
            variables[name] ?: variables[name.toLowerCase()]
        }
    }

    @PostConstruct
    void init() {
        if (init) return
        init = true
        Thread.start {
            //noinspection GroovyInfiniteLoopStatement
            while (true) {
                try {
                    refresh()
                    sleep(60 * 1000)
                } catch (Throwable t) {
                    log.error("刷新权限失败", t)
                    sleep(5 * 1000)
                }
            }
        }
    }

    private RBAC execute() {
        def rbac = new RBAC()

        def binding = new RBACBinding([
                rbac: rbac
        ])

        def importer = new ImportCustomizer()
        // importer.addStaticStars("")

        def config = new CompilerConfiguration()
        config.addCompilationCustomizers importer
        config.scriptBaseClass = RBACBaseScript.name

        def astTrans = new ASTTransformationCustomizer(new RBACDefinitionTransform())
        config.addCompilationCustomizers astTrans

        // FIXME 每次执行产生的新 Class 对象会产生内存泄漏
        def shell = new GroovyShell(binding, config)
        shell.evaluate(new URI(uri))

        rbac
    }

    private refresh() {
        def users = userService.getAllUsers().collectEntries{[(it.casUsername): it] }
        if (users) {
            this.allUsers = users
        }

        rbac = execute()

        log.debug rbac.toString()

        def isUser = { it.id && it.scope == UserScope.user }
        def isDepartment = { it.id && it.scope == UserScope.department }

        Map<String, Set<Api>> departmentRoles = [:] // 部门 id: 权限集合
        Map<String, Set<Api>> userRoles = [:] // 用户 caseName: 权限集合

        // 1. 加载默认用户角色
        def defRole = rbac.roles.find { it && it.id == DEFAULT_ROLE_KEY }
        if (defRole) {
            this.defaultRole = defRole.permissions.findAll { it && it instanceof Api }.collect { it as Api }.toSet()
        }

        // 2. 部门权限汇总
        rbac.users.findAll(isDepartment).each {
            def apis = it.roles.permissions.flatten().findAll { it && it instanceof Api }.collect { it as Api }
            departmentRoles.get(it.id, new HashSet<Api>()).addAll(apis)
        }

        // 3. 部门组权限汇总
        rbac.userGroups.each { ug ->
            def groupApis = ug.roles.permissions.flatten().findAll { it && it instanceof Api }.collect { it as Api }
            ug.users.findAll(isDepartment).each {
                departmentRoles.get(it.id, new HashSet<Api>()).addAll(groupApis)
            }
        }

        // 4. 用户权限汇总, 每个 User
        // 1) 获得其所属 Role 的所有 Permissions
        // 2) 获取默认 Role 的所有 Permissions
        // 3) 获取所隶属 Department 的所有 Permissions
        rbac.users.findAll(isUser).each {
            def apis = it.roles.permissions.flatten().findAll { it && it instanceof Api }.collect { it as Api }
            def set = userRoles.get(it.id, new HashSet<Api>())
            set.addAll(apis) // 附加组权限
            set.addAll(defaultRole) // 附加所有默认权限
            // 附加用户所属部门权限
            def departmentId = allUsers.get(it.id)?.departmentId ?: 0
            def departmentApis = departmentRoles.get(departmentId as String, new HashSet<Api>())
            set.addAll(departmentApis)
        }

        // 5. 用户组权限汇总, UserGroup 中 每个User,
        // 1) 获取该 UserGroup 中所有 Role 的 Permissions
        // 2) 获取默认 Role 的所有 Permissions
        // 3) 获取所隶属 Department 的所有 Permissions
        rbac.userGroups.each { ug ->
            // 组权限
            def groupApis = ug.roles.permissions.flatten().findAll { it && it instanceof Api }.collect { it as Api }
            ug.users.findAll(isUser).each {
                def set = userRoles.get(it.id, new HashSet<Api>())
                set.addAll(groupApis) // 附加组权限
                set.addAll(defaultRole) // 附加所有默认权限
                // 附加用户所属部门权限
                def departmentId = allUsers.get(it.id)?.departmentId ?: 0
                def departmentApis = departmentRoles.get(departmentId as String, new HashSet<Api>())
                set.addAll(departmentApis)
            }
        }

        log.debug userRoles.toString()

        if (userRoles) {
            this.userRoles = userRoles
        }
    }

    private static createCachedMatcher() {
        // clear cache
        this.&doMatch.memoizeAtMost(MAX_CACHE_SZ)
    }

    private static boolean doMatch(Set<Api> permissions, HttpMethod method, String path) {
        // 依次检查
        next:
        for (Api api: permissions) {
            // 若配置限制方法, 方法不匹配, 继续下一个 api 规则
            if (api.methods) {
                if (!api.methods.find {it == method}) {
                    continue
                }
            }

            // 如果当前 api 配置排查 path 则继续下一个  api 规则
            for (String exclude: api.exclude) {
                if (pathMatcher.match(exclude, path)) {
                    continue next
                }
            }

            // 如果当前 api 匹配 path, 返回 true 表示有权限访问
            for (String pattern: api.path) {
                if (pathMatcher.match(pattern, path)) {
                    return true
                }
            }
        }

        false
    }

    // 用户不存在, 按默认权限处理
    Set<Api> getUserPermissions(String casName) {
        userRoles.get(casName, defaultRole)
    }

    boolean isPermitted(HttpServletRequest request, String casUser) {
        if (casUser == null || casUser.isAllWhitespace()) {
            log.error("malformed cas username:{}", casUser)
            return false
        }

        def path = request.getServletPath() ?: "/"
        def httpMethod = HttpMethod.valueOf(request.getMethod().toUpperCase())
        def perms = getUserPermissions(casUser)

        matcher(perms, httpMethod, path)
    }
}
