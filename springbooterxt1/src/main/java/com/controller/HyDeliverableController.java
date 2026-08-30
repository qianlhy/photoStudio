package com.controller;

import com.annotation.IgnoreAuth;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.entity.HyCustomerEntity;
import com.entity.HyDeliverableEntity;
import com.entity.HyOrderEntity;
import com.service.impl.HyCustomerServiceImpl;
import com.service.impl.HyDeliverableServiceImpl;
import com.service.impl.HyOrderServiceImpl;
import com.utils.HyId;
import com.utils.MPUtil;
import com.utils.PageUtils;
import com.utils.Query;
import com.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/hyDeliverable")
public class HyDeliverableController {

    @Autowired
    private HyDeliverableServiceImpl service;
    @Autowired
    private HyOrderServiceImpl orderService;
    @Autowired
    private HyCustomerServiceImpl customerService;

    @IgnoreAuth
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HyDeliverableEntity entity) {
        EntityWrapper<HyDeliverableEntity> ew = new EntityWrapper<>();
        ew.orderBy("addtime", false);
        PageUtils page = new PageUtils(service.selectPage(new Query<HyDeliverableEntity>(params).getPage(),
                MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, entity), params), params)));
        return R.ok().put("data", page);
    }

    @IgnoreAuth
    @RequestMapping("/list")
    public R list(HyDeliverableEntity entity) {
        EntityWrapper<HyDeliverableEntity> ew = new EntityWrapper<>();
        ew.orderBy("addtime", false);
        return R.ok().put("data", service.selectList(MPUtil.likeOrEq(ew, entity)));
    }

    @IgnoreAuth
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        return R.ok().put("data", service.selectById(id));
    }

    /**
     * 上传成品：关联客户/订单后，小程序「我的内容」按 customerId 可查可下。
     */
    @PostMapping("/save")
    public R save(@RequestBody HyDeliverableEntity entity) {
        if (entity.getVideo() == null || entity.getVideo().trim().isEmpty()) {
            return R.error("请上传成品视频");
        }
        if (entity.getCustomerId() == null && entity.getOrderId() == null) {
            return R.error("请选择客户或关联订单");
        }
        fillFromOrderAndCustomer(entity);
        if (entity.getCustomerId() == null) {
            return R.error("未能确定客户，请选择客户或订单");
        }
        entity.setId(HyId.next());
        entity.setAddtime(new Date());
        if (entity.getStatus() == null || entity.getStatus().trim().isEmpty()) {
            entity.setStatus("上架");
        }
        if (entity.getViewStatus() == null) entity.setViewStatus("未查看");
        if (entity.getDownloadStatus() == null) entity.setDownloadStatus("未下载");
        if (entity.getQualityFlag() == null) entity.setQualityFlag(0);
        if (entity.getTitle() == null || entity.getTitle().trim().isEmpty()) {
            entity.setTitle((entity.getCustomerName() == null ? "成品" : entity.getCustomerName()) + " · 交付视频");
        }
        service.insert(entity);
        syncOrderProgress(entity.getOrderId());
        return R.ok().put("id", entity.getId());
    }

    @RequestMapping("/update")
    public R update(@RequestBody HyDeliverableEntity entity) {
        service.updateById(entity);
        if (entity.getOrderId() != null) {
            syncOrderProgress(entity.getOrderId());
        }
        return R.ok();
    }

    /** 客户标记已下载 */
    @IgnoreAuth
    @RequestMapping("/download/{id}")
    public R download(@PathVariable("id") Long id) {
        HyDeliverableEntity e = service.selectById(id);
        if (e == null) return R.error("作品不存在");
        e.setDownloadStatus("已下载");
        e.setViewStatus("已查看");
        e.setDownloadTime(new Date());
        service.updateById(e);
        return R.ok();
    }

    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        List<HyDeliverableEntity> list = service.selectBatchIds(Arrays.asList(ids));
        service.deleteBatchIds(Arrays.asList(ids));
        if (list != null) {
            for (HyDeliverableEntity d : list) {
                if (d.getOrderId() != null) syncOrderProgress(d.getOrderId());
            }
        }
        return R.ok();
    }

    private void fillFromOrderAndCustomer(HyDeliverableEntity entity) {
        if (entity.getOrderId() != null) {
            HyOrderEntity order = orderService.selectById(entity.getOrderId());
            if (order != null) {
                if (entity.getOrderNo() == null || entity.getOrderNo().trim().isEmpty()) {
                    entity.setOrderNo(order.getOrderNo());
                }
                if (entity.getCustomerId() == null) entity.setCustomerId(order.getCustomerId());
                if (entity.getCustomerName() == null || entity.getCustomerName().trim().isEmpty()) {
                    entity.setCustomerName(order.getCustomerName());
                }
                if ((entity.getIndustry() == null || entity.getIndustry().trim().isEmpty())
                        && order.getIndustry() != null) {
                    entity.setIndustry(order.getIndustry());
                }
                if ((entity.getShooterName() == null || entity.getShooterName().trim().isEmpty())
                        && order.getShooterName() != null) {
                    entity.setShooterName(order.getShooterName());
                }
                if ((entity.getEditorName() == null || entity.getEditorName().trim().isEmpty())
                        && order.getEditorName() != null) {
                    entity.setEditorName(order.getEditorName());
                }
            }
        }
        if (entity.getCustomerId() != null) {
            HyCustomerEntity c = customerService.selectById(entity.getCustomerId());
            if (c != null) {
                if (entity.getCustomerName() == null || entity.getCustomerName().trim().isEmpty()) {
                    entity.setCustomerName(c.getName());
                }
                if (entity.getIndustry() == null || entity.getIndustry().trim().isEmpty()) {
                    entity.setIndustry(c.getIndustry());
                }
            }
        }
    }

    /** 按该订单已上传成品数回写订单完成进度 */
    private void syncOrderProgress(Long orderId) {
        if (orderId == null) return;
        HyOrderEntity order = orderService.selectById(orderId);
        if (order == null) return;
        int done = service.selectCount(new EntityWrapper<HyDeliverableEntity>().eq("order_id", orderId));
        int video = order.getVideoCount() == null ? 0 : order.getVideoCount();
        if (video > 0 && done > video) done = video;
        order.setCompletedCount(done);
        String status = order.getStatus();
        if (video > 0 && done >= video) {
            order.setStatus("已完成");
        } else if (done > 0 && ("待拍摄".equals(status) || status == null || status.isEmpty())) {
            order.setStatus("待交付");
        }
        orderService.updateById(order);
    }
}
