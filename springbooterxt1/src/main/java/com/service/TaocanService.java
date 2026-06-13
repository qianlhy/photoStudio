package com.service;

import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.IService;
import com.entity.TaocanEntity;
import com.entity.view.TaocanView;
import com.entity.vo.TaocanVO;
import com.utils.PageUtils;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 套餐卡片
 */
public interface TaocanService extends IService<TaocanEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<TaocanVO> selectListVO(Wrapper<TaocanEntity> wrapper);

    TaocanVO selectVO(@Param("ew") Wrapper<TaocanEntity> wrapper);

    List<TaocanView> selectListView(Wrapper<TaocanEntity> wrapper);

    TaocanView selectView(@Param("ew") Wrapper<TaocanEntity> wrapper);

    PageUtils queryPage(Map<String, Object> params, Wrapper<TaocanEntity> wrapper);
}
