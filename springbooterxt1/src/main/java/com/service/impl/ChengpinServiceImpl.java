package com.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.dao.ChengpinDao;
import com.entity.ChengpinEntity;
import com.entity.view.ChengpinView;
import com.entity.vo.ChengpinVO;
import com.service.ChengpinService;
import com.utils.PageUtils;
import com.utils.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("chengpinService")
public class ChengpinServiceImpl extends ServiceImpl<ChengpinDao, ChengpinEntity> implements ChengpinService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Page<ChengpinEntity> page = this.selectPage(
                new Query<ChengpinEntity>(params).getPage(),
                new EntityWrapper<ChengpinEntity>()
        );
        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Wrapper<ChengpinEntity> wrapper) {
        Page<ChengpinView> page = new Query<ChengpinView>(params).getPage();
        page.setRecords(baseMapper.selectListView(page, wrapper));
        return new PageUtils(page);
    }

    @Override
    public List<ChengpinVO> selectListVO(Wrapper<ChengpinEntity> wrapper) {
        return baseMapper.selectListVO(wrapper);
    }

    @Override
    public ChengpinVO selectVO(Wrapper<ChengpinEntity> wrapper) {
        return baseMapper.selectVO(wrapper);
    }

    @Override
    public List<ChengpinView> selectListView(Wrapper<ChengpinEntity> wrapper) {
        return baseMapper.selectListView(wrapper);
    }

    @Override
    public ChengpinView selectView(Wrapper<ChengpinEntity> wrapper) {
        return baseMapper.selectView(wrapper);
    }
}
