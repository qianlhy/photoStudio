package com.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.entity.ChengpinEntity;
import com.entity.view.ChengpinView;
import com.entity.vo.ChengpinVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 成品
 */
public interface ChengpinDao extends BaseMapper<ChengpinEntity> {

    List<ChengpinVO> selectListVO(@Param("ew") Wrapper<ChengpinEntity> wrapper);

    ChengpinVO selectVO(@Param("ew") Wrapper<ChengpinEntity> wrapper);

    List<ChengpinView> selectListView(@Param("ew") Wrapper<ChengpinEntity> wrapper);

    List<ChengpinView> selectListView(Pagination page, @Param("ew") Wrapper<ChengpinEntity> wrapper);

    ChengpinView selectView(@Param("ew") Wrapper<ChengpinEntity> wrapper);
}
