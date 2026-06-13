package com.entity.view;

import com.baomidou.mybatisplus.annotations.TableName;
import com.entity.MessageEntity;
import org.apache.commons.beanutils.BeanUtils;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

/**
 * 系统消息
 * 后端返回视图实体辅助类
 */
@TableName("message")
public class MessageView extends MessageEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    public MessageView() {
    }

    public MessageView(MessageEntity messageEntity) {
        try {
            BeanUtils.copyProperties(this, messageEntity);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
