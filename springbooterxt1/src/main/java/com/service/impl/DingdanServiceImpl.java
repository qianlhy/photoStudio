package com.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.dao.DingdanDao;
import com.entity.DingdanEntity;
import com.entity.EIException;
import com.entity.view.DingdanView;
import com.entity.vo.DingdanVO;
import com.service.DingdanService;
import com.utils.OrderStatus;
import com.utils.PageUtils;
import com.utils.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service("dingdanService")
public class DingdanServiceImpl extends ServiceImpl<DingdanDao, DingdanEntity> implements DingdanService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Page<DingdanEntity> page = this.selectPage(
                new Query<DingdanEntity>(params).getPage(),
                new EntityWrapper<DingdanEntity>()
        );
        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Wrapper<DingdanEntity> wrapper) {
        Page<DingdanView> page = new Query<DingdanView>(params).getPage();
        page.setRecords(baseMapper.selectListView(page, wrapper));
        return new PageUtils(page);
    }

    @Override
    public List<DingdanVO> selectListVO(Wrapper<DingdanEntity> wrapper) {
        return baseMapper.selectListVO(wrapper);
    }

    @Override
    public DingdanVO selectVO(Wrapper<DingdanEntity> wrapper) {
        return baseMapper.selectVO(wrapper);
    }

    @Override
    public List<DingdanView> selectListView(Wrapper<DingdanEntity> wrapper) {
        return baseMapper.selectListView(wrapper);
    }

    @Override
    public DingdanView selectView(Wrapper<DingdanEntity> wrapper) {
        return baseMapper.selectView(wrapper);
    }

    @Override
    @Transactional
    public DingdanEntity createOrder(DingdanEntity dingdan) {
        int activeCount = this.selectCount(new EntityWrapper<DingdanEntity>()
                .notIn("zhuangtai", OrderStatus.INACTIVE));
        dingdan.setPaiduixuhao(activeCount + 1);
        this.insert(dingdan);
        return dingdan;
    }

    @Override
    @Transactional
    public void cancelOrder(Long id, String reason) {
        DingdanEntity dingdan = this.selectById(id);
        if (dingdan == null) {
            throw new EIException("订单不存在", 404);
        }
        if (!OrderStatus.canCancel(dingdan.getZhuangtai())) {
            throw new EIException("当前状态（" + dingdan.getZhuangtai() + "）不可取消");
        }
        dingdan.setZhuangtai(OrderStatus.CANCELLED);
        dingdan.setQuxiaoyuanyin(reason);
        this.updateById(dingdan);
    }

    @Override
    @Transactional
    public void updateChecked(DingdanEntity dingdan) {
        DingdanEntity old = this.selectById(dingdan.getId());
        if (old == null) {
            throw new EIException("订单不存在", 404);
        }
        if (!OrderStatus.canTransfer(old.getZhuangtai(), dingdan.getZhuangtai())) {
            throw new EIException("非法状态流转：" + old.getZhuangtai() + " → " + dingdan.getZhuangtai());
        }
        this.updateById(dingdan);
    }

    @Override
    @Transactional
    public boolean markPaid(String dingdanbianhao) {
        DingdanEntity order = this.selectOne(new EntityWrapper<DingdanEntity>()
                .eq("dingdanbianhao", dingdanbianhao));
        if (order == null) {
            throw new EIException("订单不存在", 404);
        }
        if ("已缴费锁定档期".equals(order.getJiaofeisuoding())) {
            return false;
        }
        order.setJiaofeisuoding("已缴费锁定档期");
        this.updateById(order);
        return true;
    }
}
