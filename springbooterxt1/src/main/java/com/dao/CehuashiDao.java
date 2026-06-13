package com.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.entity.CehuashiEntity;
import com.entity.view.CehuashiView;
import com.entity.vo.CehuashiVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 策划师
 *
 * @author
 * @email

 */
public interface CehuashiDao extends BaseMapper<CehuashiEntity> {

    List<CehuashiVO> selectListVO(@Param("ew") Wrapper<CehuashiEntity> wrapper);

    CehuashiVO selectVO(@Param("ew") Wrapper<CehuashiEntity> wrapper);

    List<CehuashiView> selectListView(@Param("ew") Wrapper<CehuashiEntity> wrapper);

    List<CehuashiView> selectListView(Pagination page, @Param("ew") Wrapper<CehuashiEntity> wrapper);

    CehuashiView selectView(@Param("ew") Wrapper<CehuashiEntity> wrapper);


}
