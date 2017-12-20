
### TODO

1. 执行流行说明, 文档
2. 规则版本号, 开关

1. 配置简易数据源

```
<bean id="simpleRuleDatasource" class="com.mysql.jdbc.jdbc2.optional.MysqlDataSource">
    <property name="url" value="jdbc:mysql://127.0.0.1:3306/et_engine?useServerPrepStmts=false&amp;zeroDateTimeBehavior=convertToNull&amp;characterEncoding=utf8" />
    <property name="user" value="root" />
    <property name="password" value="123456" />
</bean>
```

或者druid连接池

```
    <bean id="abstractDataSource" class="com.alibaba.druid.pool.DruidDataSource" abstract="true">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="connectionInitSqls" value="set names utf8mb4"/>
        <property name="initialSize" value="5" />
        <property name="maxActive" value="20" />
        <property name="minIdle" value="5" />
        <property name="maxWait" value="5000" />
        <property name="phyTimeoutMillis" value="7200000"/>
        <property name="removeAbandoned" value="true"/>
        <property name="removeAbandonedTimeout" value="180"/>
        <property name="testWhileIdle" value="true"/>
        <property name="testOnBorrow" value="false"/>
        <property name="testOnReturn" value="false"/>
        <property name="validationQuery" value="SELECT 1"/>
        <property name="validationQueryTimeout" value="1"/>
        <property name="timeBetweenEvictionRunsMillis" value="15000"/>
        <property name="minEvictableIdleTimeMillis" value="300000"/>
        <property name="maxEvictableIdleTimeMillis" value="300000"/>
        <property name="defaultAutoCommit" value="true"/>
        <property name="logAbandoned" value="true"/>
        <property name="connectionProperties" value="socketTimeout=3000;connectTimeout=1000"/>
        <property name="proxyFilters">
            <list>
                <ref bean="log-filter"/>
            </list>
        </property>
    </bean>

    <bean id="ruleDataSource" class="com.alibaba.druid.pool.DruidDataSource" init-method="init" destroy-method="close" parent="abstractDataSource">
        <property name="name" value="#Rule#"/>
        <property name="url" value="jdbc:mysql://127.0.0.1:3306/et_engine?useServerPrepStmts=false&amp;zeroDateTimeBehavior=convertToNull&amp;characterEncoding=utf8"/>
        <property name="username" value="root"/>
        <property name="password" value="123456"/>
    </bean>
```

2. 配置 RuleEngineX

```
    <bean id="ruleEngineX" class="com.youzan.et.groovy.rulex.RuleEngineX" init-method="refresh">
        <property name="dataSource" ref="ruleDataSource"/>
        <property name="appName" value="et_xiaolv"/>
    </bean>
```