package com.yyds.hrcscommon.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @Description: 用户上下文工具类
 * @Author: yds
 * @Date:
 */
public class UserContext {
    //private static final ThreadLocal<String> userId = new ThreadLocal<>();

    /**
     * 从上下文获取当前用户ID
     * @return
     */
    public static String getCurrentUserId() {
        Authentication  authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getPrincipal().toString();
        }
        return null;
    }
    /**
     * 从请求属性获得用户ID
     */
    public static String getCurrentUserIdFromRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if(requestAttributes instanceof ServletRequestAttributes){
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            return (String) request.getAttribute("userID");
        }
        return null;
    }
}
