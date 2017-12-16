## Simple RBAC DSL

### 1. 关键字: api, role, users, group

* **api(#权限#)** 这里指 Rest 接口
* **role(#角色#)** 包含 role 有权访的 N 个 api
* **user(#用户#)**  包含 N 个 role
* **users(#用户组#)** 包含 N 个 user,  M 个 role, 表示 N 个 user 同时拥有 M 个 role
* **group** 可对 api, role, user 进行分组, 为同构类型, 可嵌套, 方便管理

### 2. 语法

```
Keyword ID {
    Key1  Value1 [, Value2, ...]
    Key2  Value1 [, Value2, ...]
    ...
}
```

或者

```
Keyword {
    id    ID
    Key1  Value1 [, Value2, ...]
    Key2  Value1 [, Value2, ...]
    ...
}
```

注意: 
1. 其中 ID 表示该 Keyword 定义实体的名称, 需要全局唯一, 用以实体组合引用
2. 其中 Key 可重复, value 类型为列表, 语义为添加, value 类型为标量, 语义为覆盖

### 3. 属性

#### 1. api

> method: list&lt;method&gt;
> path: list&lt;string&gt;
> exclude: list&lt;string&gt;

```
api fooApi {
    method      http请求方法,可选, 留空为不限制, 支持分组 ID
    path        mapping path , 字符串列表 (字符串需要加引号)
    exclude     exclude mapping path, 字符串列表
}
```
- 其中 path 与 exclude 均为 spring ant 风格的匹配格式, 参考 RequestMapping
- 其中 method 为 enum HttpMetod { GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS, TRACE }

```
?         匹配1个字符
*         匹配0个或多个字符
**        匹配路径中的0个或多个目录
{[a-z]+}  正则表达式[a-z]+匹配
```

e.g.
```
api bookReader {
    method GET
    path '/book'
}
api bookCreator {
    method POST
    path '/book'
}
api bookUpdater {
    method POST
    path '/book'
}
api allAboutLog {
    path '/log/**'
}
```

#### 2. role

> api:  list&lt;api|group&lt;api&gt;&gt;

```
role booRole {
    api         apiID, 支持api groupID
}
```


e.g.

```
role bookMgr {
    api bookReader
    api bookCreator, bookUpdate
}
role visitor {
    api bookReader
}
```

#### 4. user

> role list&lt;role|hroup&lt;role&gt;&gt;

> scope enum UserScope { user, department, business }

```
user xiaofeng {
    role        用户权限列表, 支持分组 ID
    scope       可选, 默认 user
}
```

e.g.

```
user xiaofeng {
    role bookMgr, visitor
}
user 'x-man' {
    role 'x-role'
}
user {
    id 'x-man'
    role 'x-role'
}
```


#### 3. users

> role list&lt;role|hroup&lt;role&gt;&gt;

> user list&lt;user|hroup&lt;user&gt;&gt;

e.g.

```
users admin {
    role bookMgr, userEditor
    user xiaofeng, foo, bar
}
```



#### 4. group

> api:  list&lt;api|group&lt;api&gt;&gt;

> role list&lt;role|group&lt;role&gt;&gt;

> user list&lt;user|group&lt;user&gt;&gt;

e.g. 

```
// group user
group adminUserGroup {
   user xiaofeng, 'x-man'
   user foo, bar
}

// group api
group mixedApi {
    api api1, api2
}

// group role
group mixedRole {
    role role1, role2
}

// 可以直接引用 group, 并且可以混合 实体和组
users someUserGroup {
    user  someUser, adminUserGroup
    role  someRole, mixedRole
}

// 组可以嵌套
group userGroupX {
    user userA, userB
}
group userGroupY {
    user userGroupX, userC
}
```

以上均支持 desc: string 属性, 为当前实体描述字段


