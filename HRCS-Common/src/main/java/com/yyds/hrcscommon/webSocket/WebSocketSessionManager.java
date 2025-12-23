package com.yyds.hrcscommon.webSocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 会话管理器
 * 维护 userId -> sessionId 映射
 */
@Slf4j
@Component
public class WebSocketSessionManager {

    // userId -> sessionId 映射（支持多设备登录，可用 Map<Long, Set<String>>）
    private final Map<Long, String> userSessions = new ConcurrentHashMap<>();

    /**
     * 用户连接时注册会话
     */
    public void registerSession(Long userId, String sessionId) {
        if (userId != null && sessionId != null) {
            userSessions.put(userId, sessionId);
            log.info("WebSocket用户连接：userId={}, sessionId={}", userId, sessionId);
        }
    }

    /**
     * 用户断开时移除会话
     */
    public void removeSession(Long userId) {
        if (userId != null) {
            userSessions.remove(userId);
            log.info("WebSocket用户断开：userId={}", userId);
        }
    }

    /**
     * 获取用户会话ID
     */
    public String getSessionId(Long userId) {
        return userSessions.get(userId);
    }

    /**
     * 判断用户是否在线
     */
    public boolean isUserOnline(Long userId) {
        return userSessions.containsKey(userId);
    }

    /**
     * 获取在线用户数
     */
    public int getOnlineCount() {
        return userSessions.size();
    }

    /**
     * 获取所有在线用户ID列表（核心方法）
     */
    public Set<Long> getAllUserIds() {
        return userSessions.keySet();
    }

    /**
     * 清空所有会话（系统维护时使用）
     */
    public void clearAllSessions() {
        userSessions.clear();
        log.warn("WebSocket所有会话已被清空");
    }
}