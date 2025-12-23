package com.yyds.hrcscommon.webSocket;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * WebSocket 业务服务
 * 提供向用户推送消息的能力
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    private final WebSocketSessionManager sessionManager;

    /**
     * 向单个用户发送消息
     * @param userId 用户ID
     * @param message 消息内容（可以是String、Map、DTO等）
     */
    public void sendToUser(Long userId, Object message) {
        if (!sessionManager.isUserOnline(userId)) {
            log.warn("用户不在线，消息发送失败：userId={}", userId);
            return;
        }

        try {
            // 发送给用户：/user/{userId}/notice
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(userId),
                    "/notice",
                    message
            );
            log.info("WebSocket消息已发送给单个用户：userId={}, message={}", userId, message);
        } catch (Exception e) {
            log.error("向用户发送消息失败：userId={}", userId, e);
        }
    }

    /**
     * 向多个用户批量发送消息（核心方法）
     * @param userIds 用户ID列表
     * @param message 消息内容
     * @return 成功发送的用户数
     */
    public  int sendToUsers(List<Long> userIds, Object message) {
        if (userIds == null || userIds.isEmpty()) {
            log.warn("用户ID列表为空，不发送消息");
            return 0;
        }

        int successCount = 0;
        List<Long> failedUsers = new ArrayList<>();

        for (Long userId : userIds) {
            if (sessionManager.isUserOnline(userId)) {
                try {
                    sendToUser(userId, message);
                    successCount++;
                } catch (Exception e) {
                    failedUsers.add(userId);
                    log.error("批量发送失败：userId={}", userId, e);
                }
            } else {
                log.debug("用户不在线，跳过：userId={}", userId);
                failedUsers.add(userId);
            }
        }

        log.info("WebSocket批量发送完成，目标：{}人，成功：{}人，失败：{}人",
                userIds.size(), successCount, failedUsers.size());

        return successCount;
    }

    /**
     * 向所有在线用户广播消息
     * @param message 消息内容
     */
    public void broadcast(Object message) {
        try {
            // 发送到主题：/topic/notice
            messagingTemplate.convertAndSend("/topic/notice", message);
            log.info("WebSocket广播消息已发送：message={}", message);
        } catch (Exception e) {
            log.error("广播消息失败", e);
        }
    }

    /**
     * 获取在线用户数
     */
    public int getOnlineUserCount() {
        return sessionManager.getOnlineCount();
    }

    /**
     * 获取在线用户列表
     */
    public List<Long> getOnlineUserIds() {
        return new ArrayList<>(sessionManager.getAllUserIds());
    }
}