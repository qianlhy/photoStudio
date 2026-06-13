package com.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.dao.TaocanDao;
import com.entity.TaocanEntity;
import com.entity.view.TaocanView;
import com.entity.vo.TaocanVO;
import com.service.TaocanService;
import com.utils.PageUtils;
import com.utils.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("taocanService")
public class TaocanServiceImpl extends ServiceImpl<TaocanDao, TaocanEntity> implements TaocanService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Page<TaocanEntity> page = this.selectPage(
                new Query<TaocanEntity>(params).getPage(),
                new EntityWrapper<TaocanEntity>()
        );
        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Wrapper<TaocanEntity> wrapper) {
        Page<TaocanView> page = new Query<TaocanView>(params).getPage();
        page.setRecords(baseMapper.selectListView(page, wrapper));
        return new PageUtils(page);
    }

    @Override
    public List<TaocanVO> selectListVO(Wrapper<TaocanEntity> wrapper) {
        return baseMapper.selectListVO(wrapper);
    }

    @Override
    public TaocanVO selectVO(Wrapper<TaocanEntity> wrapper) {
        return baseMapper.selectVO(wrapper);
    }

    @Override
    public List<TaocanView> selectListView(Wrapper<TaocanEntity> wrapper) {
        return baseMapper.selectListView(wrapper);
    }

    @Override
    public TaocanView selectView(Wrapper<TaocanEntity> wrapper) {
        return baseMapper.selectView(wrapper);
    }
}
