package com.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.entity.LeixingEntity;
import com.entity.view.LeixingView;
import com.entity.vo.LeixingVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 类型
 *
 * @author
 * @email

 */
public interface LeixingDao extends BaseMapper<LeixingEntity> {

    List<LeixingVO> selectListVO(@Param("ew") Wrapper<LeixingEntity> wrapper);

    LeixingVO selectVO(@Param("ew") Wrapper<LeixingEntity> wrapper);

    List<LeixingView> selectListView(@Param("ew") Wrapper<LeixingEntity> wrapper);

    List<LeixingView> selectListView(Pagination page, @Param("ew") Wrapper<LeixingEntity> wrapper);

    LeixingView selectView(@Param("ew") Wrapper<LeixingEntity> wrapper);


}
