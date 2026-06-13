package com.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.entity.DingdanEntity;
import com.entity.view.DingdanView;
import com.entity.vo.DingdanVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单
 */
public interface DingdanDao extends BaseMapper<DingdanEntity> {

    List<DingdanVO> selectListVO(@Param("ew") Wrapper<DingdanEntity> wrapper);

    DingdanVO selectVO(@Param("ew") Wrapper<DingdanEntity> wrapper);

    List<DingdanView> selectListView(@Param("ew") Wrapper<DingdanEntity> wrapper);

    List<DingdanView> selectListView(Pagination page, @Param("ew") Wrapper<DingdanEntity> wrapper);

    DingdanView selectView(@Param("ew") Wrapper<DingdanEntity> wrapper);
}
