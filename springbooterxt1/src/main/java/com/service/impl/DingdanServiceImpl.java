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
import com.service.MessageNotifyService;
import com.utils.OrderStatus;
import com.utils.PageUtils;
import com.utils.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service("dingdanService")
public class DingdanServiceImpl extends ServiceImpl<DingdanDao, DingdanEntity> implements DingdanService {

    @Autowired
    private MessageNotifyService messageNotifyService;

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
        int paidCount = this.selectCount(new EntityWrapper<DingdanEntity>()
                .notIn("zhuangtai", OrderStatus.INACTIVE)
                .in("jiaofeisuoding", Arrays.asList("是", "已缴费锁定档期")));
        int normalCount = this.selectCount(new EntityWrapper<DingdanEntity>()
                .notIn("zhuangtai", OrderStatus.INACTIVE)
                .notIn("jiaofeisuoding", Arrays.asList("是", "已缴费锁定档期")));
        dingdan.setPaiduixuhao(paidCount + normalCount + 1);
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
        // 档期变更通知
        if (dingdan.getYugudangqi() != null && old.getYugudangqi() != null
                && !dingdan.getYugudangqi().equals(old.getYugudangqi())) {
            String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(dingdan.getYugudangqi());
            messageNotifyService.sendSchedule(old.getUserid(), old.getDingdanbianhao(), dateStr);
        } else if (dingdan.getYugudangqi() != null && old.getYugudangqi() == null) {
            String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(dingdan.getYugudangqi());
            messageNotifyService.sendSchedule(old.getUserid(), old.getDingdanbianhao(), dateStr);
        }
        // 缴费锁档后调整到已缴费队列末尾（优先于未缴费）
        if (isPaidLock(dingdan.getJiaofeisuoding()) && !isPaidLock(old.getJiaofeisuoding())) {
            int paidMax = this.selectCount(new EntityWrapper<DingdanEntity>()
                    .notIn("zhuangtai", OrderStatus.INACTIVE)
                    .in("jiaofeisuoding", Arrays.asList("是", "已缴费锁定档期")));
            DingdanEntity patch = new DingdanEntity();
            patch.setId(dingdan.getId());
            patch.setPaiduixuhao(Math.max(1, paidMax));
            patch.setJiaofeisuoding("已缴费锁定档期");
            this.updateById(patch);
        }
    }

    private boolean isPaidLock(String v) {
        return "是".equals(v) || "已缴费锁定档期".equals(v);
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
