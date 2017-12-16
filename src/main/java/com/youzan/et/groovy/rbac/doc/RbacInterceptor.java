//package com.youzan.et.groovy.rbac;
//
//import com.alibaba.fastjson.JSONObject;
//import com.alibaba.fastjson.serializer.SerializerFeature;
//import com.youzan.et.xiaolv.common.enums.ResultCode;
//import com.youzan.et.xiaolv.common.model.RestResult;
//import com.youzan.et.xiaolv.model.SessionUser;
//import lombok.extern.slf4j.Slf4j;
//
//import javax.annotation.Resource;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//@Slf4j
//class RbacInterceptor extends BaseInterceptor {
//
//    @Resource
//    private
//    RBACService rbacService;
//
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
//            throws Exception {
//        if (bypass(request)) {
//            return true;
//        }
//
//        SessionUser user = (SessionUser) request.getAttribute(SessionInterceptor.SESSION_ATTR_KEY);
//        boolean permitted = rbacService.isPermitted(request, user);
//        if (!permitted) {
//            RestResult<String> result = new RestResult<>(ResultCode.CHECK_PERMISSION_ERROR);
//            response.setHeader("Content-type", "text/html;charset=UTF-8");
//            response.setCharacterEncoding("UTF-8");
//            response.getWriter().write(JSONObject.toJSONString(result, SerializerFeature.WriteNullStringAsEmpty));
//            response.getWriter().flush();
//            log.error("无权访问 路径={} 来源={}", request.getServletPath(), request.getHeader("X-Real-IP"));
//        }
//        return permitted;
//    }
//
//}
