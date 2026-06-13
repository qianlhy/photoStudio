package com.service;

import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.IService;
import com.entity.DingdanEntity;
import com.entity.view.DingdanView;
import com.entity.vo.DingdanVO;
import com.utils.PageUtils;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 订单
 */
public interface DingdanService extends IService<DingdanEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<DingdanVO> selectListVO(Wrapper<DingdanEntity> wrapper);

    DingdanVO selectVO(@Param("ew") Wrapper<DingdanEntity> wrapper);

    List<DingdanView> selectListView(Wrapper<DingdanEntity> wrapper);

    DingdanView selectView(@Param("ew") Wrapper<DingdanEntity> wrapper);

    PageUtils queryPage(Map<String, Object> params, Wrapper<DingdanEntity> wrapper);

    /**
     * 事务化下单：在事务内原子计算排队序号并落库（配合上层串行锁防止并发重号）。
     */
    DingdanEntity createOrder(DingdanEntity dingdan);

    /**
     * 取消订单（带状态机校验，仅允许未完成、未取消的订单取消）。
     */
    void cancelOrder(Long id, String reason);

    /**
     * 后台更新订单：对状态变更做合法流转校验后保存。
     */
    void updateChecked(DingdanEntity dingdan);

    /**
     * 支付成功后标记订单为“已缴费锁定档期”（幂等：重复回调不重复处理）。
     * @return 本次是否产生状态变更
     */
    boolean markPaid(String dingdanbianhao);
}
