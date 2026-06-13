package com.service;

/**
 * 模板化消息通知：读取 config 中 msgTpl* 配置并写入 message 表。
 */
public interface MessageNotifyService {

    void sendAudit(Long userId, String result, String reason);

    void sendFinish(Long userId, String orderNo);

    void sendSchedule(Long userId, String orderNo, String date);

    void sendActivity(Long userId, String title, String content);

    void sendFeedback(Long userId, String orderNo, String content);
}
