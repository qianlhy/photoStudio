package com.service;

import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.IService;
import com.entity.LeixingEntity;
import com.entity.view.LeixingView;
import com.entity.vo.LeixingVO;
import com.utils.PageUtils;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


/**
 * 类型
 *
 * @author
 * @email

 */
public interface LeixingService extends IService<LeixingEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<LeixingVO> selectListVO(Wrapper<LeixingEntity> wrapper);

    LeixingVO selectVO(@Param("ew") Wrapper<LeixingEntity> wrapper);

    List<LeixingView> selectListView(Wrapper<LeixingEntity> wrapper);

    LeixingView selectView(@Param("ew") Wrapper<LeixingEntity> wrapper);

    PageUtils queryPage(Map<String, Object> params, Wrapper<LeixingEntity> wrapper);


}

