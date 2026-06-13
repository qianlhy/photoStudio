package com.service;

import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.IService;
import com.entity.MessageEntity;
import com.entity.view.MessageView;
import com.entity.vo.MessageVO;
import com.utils.PageUtils;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 系统消息
 */
public interface MessageService extends IService<MessageEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<MessageVO> selectListVO(Wrapper<MessageEntity> wrapper);

    MessageVO selectVO(@Param("ew") Wrapper<MessageEntity> wrapper);

    List<MessageView> selectListView(Wrapper<MessageEntity> wrapper);

    MessageView selectView(@Param("ew") Wrapper<MessageEntity> wrapper);

    PageUtils queryPage(Map<String, Object> params, Wrapper<MessageEntity> wrapper);
}
