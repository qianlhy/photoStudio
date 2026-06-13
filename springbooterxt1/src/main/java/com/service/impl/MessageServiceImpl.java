package com.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.dao.MessageDao;
import com.entity.MessageEntity;
import com.entity.view.MessageView;
import com.entity.vo.MessageVO;
import com.service.MessageService;
import com.utils.PageUtils;
import com.utils.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("messageService")
public class MessageServiceImpl extends ServiceImpl<MessageDao, MessageEntity> implements MessageService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Page<MessageEntity> page = this.selectPage(
                new Query<MessageEntity>(params).getPage(),
                new EntityWrapper<MessageEntity>()
        );
        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Wrapper<MessageEntity> wrapper) {
        Page<MessageView> page = new Query<MessageView>(params).getPage();
        page.setRecords(baseMapper.selectListView(page, wrapper));
        return new PageUtils(page);
    }

    @Override
    public List<MessageVO> selectListVO(Wrapper<MessageEntity> wrapper) {
        return baseMapper.selectListVO(wrapper);
    }

    @Override
    public MessageVO selectVO(Wrapper<MessageEntity> wrapper) {
        return baseMapper.selectVO(wrapper);
    }

    @Override
    public List<MessageView> selectListView(Wrapper<MessageEntity> wrapper) {
        return baseMapper.selectListView(wrapper);
    }

    @Override
    public MessageView selectView(Wrapper<MessageEntity> wrapper) {
        return baseMapper.selectView(wrapper);
    }
}
