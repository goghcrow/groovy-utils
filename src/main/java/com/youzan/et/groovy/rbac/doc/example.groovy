package com.youzan.et.groovy.rbac.doc

api visitorApi {
    path '/**'
    exclude '/demand/move'
}

api demandCreateApi {
    method POST
    path '/demand/create'
}

api demandEditApi {
    method POST
    path '/demand/edit'
}

api demandDeleteApi {
    method POST
    path '/demand/delete'
}

api demandMoveApi {
    path '/demand/move'
}

api demandSearchApi {
    path '/demand/search'
}

api demandGetListApi {
    method GET
    path '/demand/getList'
}

api demandGetMyListApi {
    method GET
    path '/demand/getMyList'
}

api demandLogListApi {
    path '/demand/log/list'
}


group demandEditorApiGroup {
    api demandCreateApi, demandEditApi
    api demandMoveApi,demandDeleteApi
}

group demandReaderApiGroup {
    api demandSearchApi,demandGetListApi
    api demandGetMyListApi ,demandLogListApi
}


role demandEditorRole {
    api demandReaderApiGroup, demandEditorApiGroup
}

role demandReaderRole {
    api demandReaderApiGroup
}

group tlUserGroup {
    user user8,user9,user10
}
group xiaolvOpUserGroup {
    user user2,user3, user4, user5,user7, user6
}
group xiaolvDevelpersUserGroup {
    user chuxiaofeng, user1
}

users demandEditorUsers {
    user xiaolvDevelpersUserGroup,xiaolvOpUserGroup,tlUserGroup
    role demandEditorRole
}


// 默认用户角色
role missing {
    api visitorApi
}