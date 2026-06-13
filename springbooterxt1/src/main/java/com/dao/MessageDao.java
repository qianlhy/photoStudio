package com.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.entity.MessageEntity;
import com.entity.view.MessageView;
import com.entity.vo.MessageVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统消息
 */
public interface MessageDao extends BaseMapper<MessageEntity> {

    List<MessageVO> selectListVO(@Param("ew") Wrapper<MessageEntity> wrapper);

    MessageVO selectVO(@Param("ew") Wrapper<MessageEntity> wrapper);

    List<MessageView> selectListView(@Param("ew") Wrapper<MessageEntity> wrapper);

    List<MessageView> selectListView(Pagination page, @Param("ew") Wrapper<MessageEntity> wrapper);

    MessageView selectView(@Param("ew") Wrapper<MessageEntity> wrapper);
}
