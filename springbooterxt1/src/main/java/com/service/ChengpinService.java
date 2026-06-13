package com.service;

import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.IService;
import com.entity.ChengpinEntity;
import com.entity.view.ChengpinView;
import com.entity.vo.ChengpinVO;
import com.utils.PageUtils;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 成品
 */
public interface ChengpinService extends IService<ChengpinEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<ChengpinVO> selectListVO(Wrapper<ChengpinEntity> wrapper);

    ChengpinVO selectVO(@Param("ew") Wrapper<ChengpinEntity> wrapper);

    List<ChengpinView> selectListView(Wrapper<ChengpinEntity> wrapper);

    ChengpinView selectView(@Param("ew") Wrapper<ChengpinEntity> wrapper);

    PageUtils queryPage(Map<String, Object> params, Wrapper<ChengpinEntity> wrapper);
}
