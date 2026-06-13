package com.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.entity.TaocanEntity;
import com.entity.view.TaocanView;
import com.entity.vo.TaocanVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 套餐卡片
 */
public interface TaocanDao extends BaseMapper<TaocanEntity> {

    List<TaocanVO> selectListVO(@Param("ew") Wrapper<TaocanEntity> wrapper);

    TaocanVO selectVO(@Param("ew") Wrapper<TaocanEntity> wrapper);

    List<TaocanView> selectListView(@Param("ew") Wrapper<TaocanEntity> wrapper);

    List<TaocanView> selectListView(Pagination page, @Param("ew") Wrapper<TaocanEntity> wrapper);

    TaocanView selectView(@Param("ew") Wrapper<TaocanEntity> wrapper);
}
