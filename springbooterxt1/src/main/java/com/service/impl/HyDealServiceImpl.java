package com.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.entity.HyActionItemEntity;
import com.entity.HyContentItemEntity;
import com.entity.HyContentPlanEntity;
import com.entity.HyCustomerEntity;
import com.entity.HyMaterialEntity;
import com.entity.HyOrderEntity;
import com.utils.HyId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 合意主闭环：方案确认 → 待付款 → 收款建单 → 库存回写；以及制作预警刷新。
 */
@Service("hyDealServiceImpl")
public class HyDealServiceImpl {

    @Autowired
    private HyCustomerServiceImpl customerService;
    @Autowired
    private HyContentPlanServiceImpl planService;
    @Autowired
    private HyOrderServiceImpl orderService;
    @Autowired
    private HyContentItemServiceImpl itemService;
    @Autowired
    private HyActionItemServiceImpl actionService;
    @Autowired
    private HyMaterialServiceImpl materialService;

    /** 选片结束后：客户进入待付款，并生成待付款行动事项 */
    @Transactional
    public void markAwaitingPayment(Long customerId, Long planId) {
        if (customerId == null) return;
        HyCustomerEntity c = customerService.selectById(customerId);
        if (c == null) return;
        c.setFollowStatus("待付款");
        Integer unpaid = c.getUnpaidCount() == null ? 0 : c.getUnpaidCount();
        c.setUnpaidCount(Math.max(1, unpaid));
        if (planId != null) {
            HyContentPlanEntity plan = planService.selectById(planId);
            if (plan != null) {
                c.setSelectedCount(plan.getTotalCount() == null ? c.getSelectedCount() : plan.getTotalCount());
                c.setPreference(buildPreference(plan));
            }
        }
        customerService.updateById(c);

        // 避免重复待付款事项
        List<HyActionItemEntity> exists = actionService.selectList(new EntityWrapper<HyActionItemEntity>()
                .eq("customer_id", customerId)
                .eq("type", "待付款")
                .ne("status", "已完成"));
        if (exists != null && !exists.isEmpty()) return;

        HyActionItemEntity a = new HyActionItemEntity();
        a.setId(HyId.next());
        a.setAddtime(new Date());
        a.setType("待付款");
        a.setCustomerId(c.getId());
        a.setCustomerName(c.getName());
        a.setTitle("内容方案已确认，等待客户付款");
        a.setOwnerId(c.getManagerId());
        a.setOwnerName(c.getManagerName());
        a.setIntention(c.getIntention());
        a.setInnerProgress(0);
        a.setCanIntervene(0);
        a.setStatus("待处理");
        a.setBelong("销售");
        actionService.insert(a);
    }

    /**
     * 付款成功后才创建订单（不设待付款状态）。
     * @return 新建订单 id
     */
    @Transactional
    public Long confirmPayAndCreateOrder(Long customerId, Long planId) {
        HyCustomerEntity c = customerService.selectById(customerId);
        if (c == null) throw new IllegalArgumentException("客户不存在");
        if (!"待付款".equals(c.getFollowStatus())) {
            throw new IllegalArgumentException("客户当前不是「待付款」状态，请先完成选片生成方案");
        }

        HyContentPlanEntity plan = resolvePlan(customerId, planId);
        if (plan == null) throw new IllegalArgumentException("请先完成选片并生成内容方案");

        // 幂等：同一方案若已有订单则直接返回
        List<HyOrderEntity> recent = orderService.selectList(new EntityWrapper<HyOrderEntity>()
                .eq("customer_id", customerId)
                .orderBy("addtime", false)
                .last("limit 5"));
        if (recent != null) {
            for (HyOrderEntity o : recent) {
                if (o.getRecipe() != null && o.getRecipe().contains("plan:" + plan.getId())) {
                    return o.getId();
                }
            }
        }

        Date now = new Date();
        int videoCount = plan.getTotalCount() == null || plan.getTotalCount() <= 0 ? 10 : plan.getTotalCount();

        HyOrderEntity order = new HyOrderEntity();
        order.setId(HyId.next());
        order.setAddtime(now);
        order.setOrderNo("YJ-" + new SimpleDateFormat("MMdd").format(now) + "-"
                + (int) (Math.random() * 900 + 100));
        order.setCustomerId(c.getId());
        order.setCustomerName(c.getName());
        order.setPackageName("门店增长套餐");
        order.setVideoCount(videoCount);
        order.setCompletedCount(0);
        order.setManagerId(c.getManagerId());
        order.setManagerName(c.getManagerName());
        order.setIndustry(c.getIndustry());
        order.setBiztype(c.getBiztype());
        order.setStatus("待拍摄");
        order.setRecipe(buildRecipe(plan) + " | plan:" + plan.getId());

        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.add(Calendar.DAY_OF_MONTH, 3);
        order.setShootDate(cal.getTime());
        cal.add(Calendar.DAY_OF_MONTH, 12);
        order.setTargetDate(cal.getTime()); // 内部 15 天周期
        cal.setTime(now);
        cal.add(Calendar.DAY_OF_MONTH, 30);
        order.setDeliverDate(cal.getTime()); // 对客承诺 30 天
        orderService.insert(order);

        createContentItems(order, plan);

        plan.setConfirmed(1);
        planService.updateById(plan);

        c.setFollowStatus("已成交");
        c.setDealCount((c.getDealCount() == null ? 0 : c.getDealCount()) + 1);
        c.setUnpaidCount(0);
        c.setRemainCount((c.getRemainCount() == null ? 0 : c.getRemainCount()) + videoCount);
        c.setPublishDays((c.getPublishDays() == null ? 0 : c.getPublishDays()) + Math.max(7, videoCount * 2));
        c.setShotCount(c.getShotCount() == null ? 0 : c.getShotCount());
        customerService.updateById(c);

        // 关闭待付款事项
        List<HyActionItemEntity> pays = actionService.selectList(new EntityWrapper<HyActionItemEntity>()
                .eq("customer_id", customerId)
                .eq("type", "待付款")
                .ne("status", "已完成"));
        if (pays != null) {
            for (HyActionItemEntity a : pays) {
                a.setStatus("已完成");
                actionService.updateById(a);
            }
        }
        return order.getId();
    }

    /** 刷新制作预警：内部周期 50% 关注，2/3 且落后才可干预 */
    @Transactional
    public int refreshProductionWarnings() {
        List<HyOrderEntity> orders = orderService.selectList(new EntityWrapper<HyOrderEntity>()
                .in("status", Arrays.asList("待拍摄", "待交付")));
        int touched = 0;
        Date now = new Date();
        if (orders == null) return 0;
        for (HyOrderEntity o : orders) {
            Date start = o.getShootDate() != null ? o.getShootDate() : o.getAddtime();
            Date end = o.getTargetDate() != null ? o.getTargetDate() : o.getDeliverDate();
            if (start == null || end == null || end.getTime() <= start.getTime()) continue;

            double timePct = (now.getTime() - start.getTime()) * 100.0 / (end.getTime() - start.getTime());
            if (timePct < 0) timePct = 0;
            if (timePct > 100) timePct = 100;
            int inner = (int) Math.round(timePct);

            int video = o.getVideoCount() == null || o.getVideoCount() == 0 ? 1 : o.getVideoCount();
            int done = o.getCompletedCount() == null ? 0 : o.getCompletedCount();
            double contentPct = done * 100.0 / video;
            boolean behind = contentPct + 5 < timePct; // 内容进度明显落后于时间进度

            if (inner < 50) {
                // 未到关注阈值：若已有未完成预警且进度回正，可保留但不开放干预
                continue;
            }

            int canIntervene = (inner >= 66 && behind) ? 1 : 0;
            String title = behind
                    ? ("制作进度落后：已完成 " + done + "/" + video + "，内部周期 " + inner + "%")
                    : ("制作周期已过半，请关注进度：已完成 " + done + "/" + video);

            List<HyActionItemEntity> exists = actionService.selectList(new EntityWrapper<HyActionItemEntity>()
                    .eq("order_id", o.getId())
                    .eq("type", "制作预警")
                    .ne("status", "已完成"));
            HyActionItemEntity a;
            if (exists != null && !exists.isEmpty()) {
                a = exists.get(0);
                a.setInnerProgress(inner);
                a.setCanIntervene(canIntervene);
                a.setTitle(title);
                if (canIntervene == 1 && !"处理中".equals(a.getStatus())) {
                    a.setStatus("待处理");
                }
                actionService.updateById(a);
            } else {
                a = new HyActionItemEntity();
                a.setId(HyId.next());
                a.setAddtime(now);
                a.setType("制作预警");
                a.setCustomerId(o.getCustomerId());
                a.setCustomerName(o.getCustomerName());
                a.setOrderId(o.getId());
                a.setTitle(title);
                a.setOwnerId(o.getManagerId());
                a.setOwnerName(o.getManagerName());
                a.setInnerProgress(inner);
                a.setCanIntervene(canIntervene);
                a.setStatus("待处理");
                a.setBelong("销售");
                actionService.insert(a);
            }
            // 同步订单异常标记
            if (canIntervene == 1) {
                o.setAbnormal("进度落后可干预");
            } else if (behind) {
                o.setAbnormal("进度关注");
            } else {
                o.setAbnormal(null);
            }
            orderService.updateById(o);
            touched++;
        }
        return touched;
    }

    private HyContentPlanEntity resolvePlan(Long customerId, Long planId) {
        if (planId != null) {
            HyContentPlanEntity p = planService.selectById(planId);
            if (p != null) return p;
        }
        List<HyContentPlanEntity> list = planService.selectList(new EntityWrapper<HyContentPlanEntity>()
                .eq("customer_id", customerId)
                .orderBy("addtime", false)
                .last("limit 1"));
        return (list == null || list.isEmpty()) ? null : list.get(0);
    }

    private void createContentItems(HyOrderEntity order, HyContentPlanEntity plan) {
        List<ItemSpec> specs = expandRecipe(plan);
        String materials = plan.getFinalMaterials() == null ? "" : plan.getFinalMaterials();
        String[] ids = materials.isEmpty() ? new String[0] : materials.split(",");
        int sort = 1;
        int matIdx = 0;
        for (ItemSpec spec : specs) {
            HyContentItemEntity it = new HyContentItemEntity();
            it.setId(HyId.next());
            it.setAddtime(new Date());
            it.setOrderId(order.getId());
            it.setTitle(spec.type + " · 第" + sort + "条");
            it.setContentType(normalizeContentType(spec.type));
            it.setStatus(0);
            it.setSort(sort);
            // 与选片一一对应，禁止 (sort % n) 循环错配
            while (matIdx < ids.length && ids[matIdx].trim().isEmpty()) matIdx++;
            if (matIdx < ids.length) {
                String mid = ids[matIdx].trim();
                matIdx++;
                try {
                    Long midL = Long.parseLong(mid);
                    it.setMaterialRef(midL);
                    HyMaterialEntity m = materialService.selectById(midL);
                    if (m != null) {
                        it.setCover(m.getCover());
                        if (m.getTitle() != null && !m.getTitle().trim().isEmpty()) {
                            it.setTitle(m.getTitle());
                        }
                        if (m.getContentType() != null && !m.getContentType().trim().isEmpty()) {
                            it.setContentType(normalizeContentType(m.getContentType()));
                        }
                    }
                } catch (NumberFormatException ignored) {
                }
            }
            itemService.insert(it);
            sort++;
        }
    }

    private List<ItemSpec> expandRecipe(HyContentPlanEntity plan) {
        fillPlanRecipeFromMaterials(plan);
        // 选片素材是真相来源：有 finalMaterials 时按素材生成，避免「五类各 1」占位
        List<ItemSpec> fromMats = specsFromFinalMaterials(plan.getFinalMaterials());
        if (!fromMats.isEmpty()) {
            return fromMats;
        }
        List<ItemSpec> list = new ArrayList<>();
        addSpecs(list, "硬广", plan.getRAd());
        addSpecs(list, "厨过程", plan.getRProcess());
        addSpecs(list, "教知识", plan.getRKnowledge());
        addSpecs(list, "说观点", plan.getROpinion());
        addSpecs(list, "讲故事", plan.getRStory());
        if (list.isEmpty()) {
            int n = plan.getTotalCount() == null || plan.getTotalCount() <= 0 ? 5 : plan.getTotalCount();
            String[] types = {"硬广", "厨过程", "教知识", "说观点", "讲故事"};
            for (int i = 0; i < n; i++) list.add(new ItemSpec(types[i % types.length]));
        }
        return list;
    }

    /** 按选片素材顺序生成清单（与选片页一致） */
    private List<ItemSpec> specsFromFinalMaterials(String materials) {
        List<ItemSpec> list = new ArrayList<>();
        if (materials == null || materials.trim().isEmpty()) return list;
        for (String part : materials.split(",")) {
            String id = part.trim();
            if (id.isEmpty()) continue;
            try {
                HyMaterialEntity m = materialService.selectById(Long.parseLong(id));
                String type = (m != null && m.getContentType() != null && !m.getContentType().isEmpty())
                        ? normalizeContentType(m.getContentType()) : "硬广";
                list.add(new ItemSpec(type));
            } catch (NumberFormatException ignored) {
            }
        }
        return list;
    }

    /** 统一五类名，兼容「硬广短视频」等写法 */
    public static String normalizeContentType(String raw) {
        if (raw == null) return "硬广";
        String t = raw.trim();
        if (t.isEmpty()) return "硬广";
        if (t.contains("硬广")) return "硬广";
        if (t.contains("厨过程") || t.contains("过程")) return "厨过程";
        if (t.contains("教知识") || t.contains("知识")) return "教知识";
        if (t.contains("说观点") || t.contains("观点")) return "说观点";
        if (t.contains("讲故事") || t.contains("故事")) return "讲故事";
        return t;
    }

    private void addSpecs(List<ItemSpec> list, String type, Integer count) {
        int n = count == null ? 0 : count;
        for (int i = 0; i < n; i++) list.add(new ItemSpec(type));
    }

    /** 方案五类配方为 0 时，按已选素材 contentType 回填 */
    public void fillPlanRecipeFromMaterials(HyContentPlanEntity plan) {
        if (plan == null) return;
        int total = n(plan.getRAd()) + n(plan.getRProcess()) + n(plan.getRKnowledge())
                + n(plan.getROpinion()) + n(plan.getRStory());
        if (total > 0) return;
        String mats = plan.getFinalMaterials();
        if (mats == null || mats.trim().isEmpty()) return;
        int ad = 0, process = 0, knowledge = 0, opinion = 0, story = 0;
        for (String part : mats.split(",")) {
            String id = part.trim();
            if (id.isEmpty()) continue;
            try {
                HyMaterialEntity m = materialService.selectById(Long.parseLong(id));
                if (m == null || m.getContentType() == null) continue;
                String t = m.getContentType();
                if (t.contains("硬广")) ad++;
                else if (t.contains("厨过程") || t.contains("过程")) process++;
                else if (t.contains("教知识") || t.contains("知识")) knowledge++;
                else if (t.contains("说观点") || t.contains("观点")) opinion++;
                else if (t.contains("讲故事") || t.contains("故事")) story++;
            } catch (NumberFormatException ignored) {
            }
        }
        plan.setRAd(ad);
        plan.setRProcess(process);
        plan.setRKnowledge(knowledge);
        plan.setROpinion(opinion);
        plan.setRStory(story);
    }

    private String buildRecipe(HyContentPlanEntity p) {
        fillPlanRecipeFromMaterials(p);
        return "硬广" + n(p.getRAd()) + " 厨过程" + n(p.getRProcess()) + " 教知识" + n(p.getRKnowledge())
                + " 说观点" + n(p.getROpinion()) + " 讲故事" + n(p.getRStory());
    }

    private String buildPreference(HyContentPlanEntity p) {
        List<String> prefs = new ArrayList<>();
        if (n(p.getRAd()) > 0) prefs.add("硬广");
        if (n(p.getRProcess()) > 0) prefs.add("厨过程");
        if (n(p.getRKnowledge()) > 0) prefs.add("教知识");
        if (n(p.getROpinion()) > 0) prefs.add("说观点");
        if (n(p.getRStory()) > 0) prefs.add("讲故事");
        return String.join(",", prefs);
    }

    private int n(Integer v) {
        return v == null ? 0 : v;
    }

    private static class ItemSpec {
        final String type;
        ItemSpec(String type) { this.type = type; }
    }
}
