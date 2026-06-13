package com.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.entity.ConfigEntity;
import com.entity.MessageEntity;
import com.service.ConfigService;
import com.service.MessageNotifyService;
import com.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service("messageNotifyService")
public class MessageNotifyServiceImpl implements MessageNotifyService {

    @Autowired
    private ConfigService configService;

    @Autowired
    private MessageService messageService;

    private String tpl(String key, String fallback) {
        ConfigEntity c = configService.selectOne(new EntityWrapper<ConfigEntity>().eq("name", key));
        return (c == null || c.getValue() == null || c.getValue().trim().isEmpty()) ? fallback : c.getValue();
    }

    private void insert(Long userId, String title, String content, String leixing) {
        if (userId == null) return;
        MessageEntity msg = new MessageEntity();
        msg.setId(new Date().getTime() + (long) (Math.random() * 1000));
        msg.setUserid(userId);
        msg.setBiaoti(title);
        msg.setNeirong(content);
        msg.setLeixing(leixing);
        msg.setIsread("否");
        messageService.insert(msg);
    }

    @Override
    public void sendAudit(Long userId, String result, String reason) {
        String content = tpl("msgTplAudit", "您的注册申请审核结果：{result}。{reason}")
                .replace("{result}", result == null ? "" : result)
                .replace("{reason}", reason == null ? "" : reason);
        insert(userId, "注册审核通知", content, "审核");
    }

    @Override
    public void sendFinish(Long userId, String orderNo) {
        String content = tpl("msgTplFinish", "您的订单 {order} 成品已上线，请前往“成品专区”在线预览或下载。")
                .replace("{order}", orderNo == null ? "" : orderNo);
        insert(userId, "成品已上线", content, "成品");
    }

    @Override
    public void sendSchedule(Long userId, String orderNo, String date) {
        String content = tpl("msgTplSchedule", "您的订单 {order} 拍摄档期已更新为 {date}，请留意到店时间。")
                .replace("{order}", orderNo == null ? "" : orderNo)
                .replace("{date}", date == null ? "待定" : date);
        insert(userId, "档期变更通知", content, "档期");
    }

    @Override
    public void sendActivity(Long userId, String title, String content) {
        insert(userId, title, content, "活动");
    }

    @Override
    public void sendFeedback(Long userId, String orderNo, String content) {
        insert(userId, "售后申请已提交", "订单 " + (orderNo == null ? "" : orderNo) + "：" + content, "售后");
    }
}
