package com.controller;

import com.annotation.IgnoreAuth;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.entity.HyContentItemEntity;
import com.entity.HyOrderEntity;
import com.service.impl.HyContentItemServiceImpl;
import com.service.impl.HyOrderServiceImpl;
import com.utils.HyId;
import com.utils.MPUtil;
import com.utils.PageUtils;
import com.utils.Query;
import com.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/hyOrder")
public class HyOrderController {

    @Autowired
    private HyOrderServiceImpl service;
    @Autowired
    private HyContentItemServiceImpl itemService;
    @Autowired
    private com.service.impl.HyCustomerServiceImpl customerService;

    @IgnoreAuth
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HyOrderEntity entity) {
        EntityWrapper<HyOrderEntity> ew = new EntityWrapper<>();
        ew.orderBy("addtime", false);
        PageUtils page = new PageUtils(service.selectPage(new Query<HyOrderEntity>(params).getPage(),
                MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, entity), params), params)));
        return R.ok().put("data", page);
    }

    @IgnoreAuth
    @RequestMapping("/list")
    public R list(HyOrderEntity entity) {
        EntityWrapper<HyOrderEntity> ew = new EntityWrapper<>();
        ew.orderBy("addtime", false);
        return R.ok().put("data", service.selectList(MPUtil.likeOrEq(ew, entity)));
    }

    @IgnoreAuth
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        return R.ok().put("data", service.selectById(id));
    }

    /** 订单详情 + 本次内容清单 */
    @IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id) {
        HyOrderEntity order = service.selectById(id);
        List<HyContentItemEntity> items = itemService.selectList(
                new EntityWrapper<HyContentItemEntity>().eq("order_id", id).orderBy("sort", true));
        return R.ok().put("data", order).put("items", items);
    }

    /**
     * 管理端补录订单。正常闭环请走 /hyCustomer/confirmPay（付款成功后建单）。
     * 禁止写入「待付款」状态。
     */
    @PostMapping("/save")
    public R save(@RequestBody HyOrderEntity entity) {
        entity.setId(HyId.next());
        entity.setAddtime(new Date());
        if (entity.getOrderNo() == null || entity.getOrderNo().isEmpty()) {
            entity.setOrderNo("YJ-" + new SimpleDateFormat("MMdd").format(new Date()) + "-"
                    + (int) (Math.random() * 900 + 100));
        }
        if (entity.getStatus() == null || "待付款".equals(entity.getStatus())) {
            entity.setStatus("待拍摄");
        }
        if (!("待拍摄".equals(entity.getStatus()) || "待交付".equals(entity.getStatus()) || "已完成".equals(entity.getStatus()))) {
            entity.setStatus("待拍摄");
        }
        service.insert(entity);
        return R.ok().put("id", entity.getId());
    }

    @RequestMapping("/update")
    public R update(@RequestBody HyOrderEntity entity) {
        if (entity == null || entity.getId() == null) {
            return R.error("订单 id 必填");
        }
        HyOrderEntity old = service.selectById(entity.getId());
        if (old == null) return R.error("订单不存在");

        if (entity.getStatus() != null) {
            if ("待付款".equals(entity.getStatus())) {
                return R.error("订单禁止写入「待付款」状态");
            }
            if (!("待拍摄".equals(entity.getStatus())
                    || "待交付".equals(entity.getStatus())
                    || "已完成".equals(entity.getStatus()))) {
                return R.error("订单状态仅支持：待拍摄 / 待交付 / 已完成");
            }
        }
        Integer video = entity.getVideoCount() != null ? entity.getVideoCount() : old.getVideoCount();
        if (entity.getCompletedCount() != null && video != null && entity.getCompletedCount() > video) {
            entity.setCompletedCount(video);
        }
        // 进度满且仍为待拍摄时，自动推进到待交付
        Integer done = entity.getCompletedCount() != null ? entity.getCompletedCount() : old.getCompletedCount();
        String status = entity.getStatus() != null ? entity.getStatus() : old.getStatus();
        if (done != null && video != null && done > 0 && done >= video && "待拍摄".equals(status)) {
            entity.setStatus("待交付");
            status = "待交付";
        }
        if (done != null && video != null && done >= video && "待交付".equals(status)
                && entity.getStatus() == null) {
            // 保持待交付，由制作侧显式点「已完成」
        }
        service.updateById(entity);

        // 回写客户交付进度
        HyOrderEntity latest = service.selectById(entity.getId());
        if (latest != null && latest.getCustomerId() != null) {
            syncCustomerFulfillment(latest);
        }
        return R.ok().put("data", latest);
    }

    /** 按订单状态回写客户拍摄/交付计数 */
    private void syncCustomerFulfillment(HyOrderEntity order) {
        try {
            com.entity.HyCustomerEntity c = customerService.selectById(order.getCustomerId());
            if (c == null) return;
            int video = order.getVideoCount() == null ? 0 : order.getVideoCount();
            int done = order.getCompletedCount() == null ? 0 : order.getCompletedCount();
            if ("待交付".equals(order.getStatus()) || "已完成".equals(order.getStatus())) {
                if (c.getShotCount() == null || c.getShotCount() < video) {
                    c.setShotCount(video);
                }
            }
            if ("已完成".equals(order.getStatus())) {
                if (c.getDeliveredCount() == null || c.getDeliveredCount() < done) {
                    c.setDeliveredCount(done);
                }
            }
            customerService.updateById(c);
        } catch (Exception ignored) {
        }
    }

    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        service.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }
}
