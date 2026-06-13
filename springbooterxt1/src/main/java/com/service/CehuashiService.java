package com.service;

import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.IService;
import com.entity.CehuashiEntity;
import com.entity.view.CehuashiView;
import com.entity.vo.CehuashiVO;
import com.utils.PageUtils;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


/**
 * 策划师
 *
 * @author
 * @email

 */
public interface CehuashiService extends IService<CehuashiEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CehuashiVO> selectListVO(Wrapper<CehuashiEntity> wrapper);

    CehuashiVO selectVO(@Param("ew") Wrapper<CehuashiEntity> wrapper);

    List<CehuashiView> selectListView(Wrapper<CehuashiEntity> wrapper);

    CehuashiView selectView(@Param("ew") Wrapper<CehuashiEntity> wrapper);

    PageUtils queryPage(Map<String, Object> params, Wrapper<CehuashiEntity> wrapper);


}

