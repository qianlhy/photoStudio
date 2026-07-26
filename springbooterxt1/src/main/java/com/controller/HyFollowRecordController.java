package com.controller;

import com.annotation.IgnoreAuth;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.entity.HyCustomerEntity;
import com.entity.HyFollowRecordEntity;
import com.entity.HyFollowTaskEntity;
import com.service.impl.HyCustomerServiceImpl;
import com.service.impl.HyFollowRecordServiceImpl;
import com.service.impl.HyFollowTaskServiceImpl;
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
import java.util.Map;

@RestController
@RequestMapping("/hyFollowRecord")
public class HyFollowRecordController {

    @Autowired
    private HyFollowRecordServiceImpl service;
    @Autowired
    private HyFollowTaskServiceImpl taskService;
    @Autowired
    private HyCustomerServiceImpl customerService;

    @IgnoreAuth
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HyFollowRecordEntity entity) {
        EntityWrapper<HyFollowRecordEntity> ew = new EntityWrapper<>();
        ew.orderBy("addtime", false);
        PageUtils page = new PageUtils(service.selectPage(new Query<HyFollowRecordEntity>(params).getPage(),
                MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, entity), params), params)));
        return R.ok().put("data", page);
    }

    @IgnoreAuth
    @RequestMapping("/list")
    public R list(HyFollowRecordEntity entity) {
        EntityWrapper<HyFollowRecordEntity> ew = new EntityWrapper<>();
        ew.orderBy("addtime", false);
        return R.ok().put("data", service.selectList(MPUtil.likeOrEq(ew, entity)));
    }

    @IgnoreAuth
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        return R.ok().put("data", service.selectById(id));
    }

    /** 保存跟进 + 规则自动摘要 + 自动创建下一步任务 + 回写客户最近跟进 */
    @PostMapping("/save")
    public R save(@RequestBody HyFollowRecordEntity entity) {
        entity.setId(HyId.next());
        Date now = new Date();
        entity.setAddtime(now);
        if (entity.getSummary() == null || entity.getSummary().isEmpty()) {
            entity.setSummary(buildSummary(entity, now));
        }
        service.insert(entity);

        // 自动创建下一步任务
        if (entity.getNextAction() != null && !entity.getNextAction().isEmpty()) {
            HyFollowTaskEntity task = new HyFollowTaskEntity();
            task.setId(HyId.next());
            task.setAddtime(now);
            task.setCustomerId(entity.getCustomerId());
            task.setCustomerName(entity.getCustomerName());
            task.setOwnerId(entity.getRecorderId());
            task.setOwnerName(entity.getRecorderName());
            task.setTaskTime(entity.getNextTime());
            task.setAction(entity.getNextAction());
            task.setStatus("待办");
            task.setSourceRecordId(entity.getId());
            taskService.insert(task);
        }

        // 回写客户档案
        if (entity.getCustomerId() != null) {
            HyCustomerEntity c = customerService.selectById(entity.getCustomerId());
            if (c != null) {
                c.setLastFollowTime(now);
                if (entity.getIntention() != null) c.setIntention(entity.getIntention());
                customerService.updateById(c);
            }
        }
        return R.ok().put("id", entity.getId()).put("summary", entity.getSummary());
    }

    @RequestMapping("/update")
    public R update(@RequestBody HyFollowRecordEntity entity) {
        service.updateById(entity);
        return R.ok();
    }

    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        service.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }

    /** 规则模板摘要（预留 AI 接口：后续可替换为大模型生成） */
    private String buildSummary(HyFollowRecordEntity e, Date now) {
        SimpleDateFormat md = new SimpleDateFormat("M月d日");
        SimpleDateFormat hm = new SimpleDateFormat("M月d日 HH:mm");
        StringBuilder sb = new StringBuilder();
        sb.append(md.format(now)).append("，");
        if (e.getRecorderName() != null) sb.append(e.getRecorderName());
        sb.append("跟进").append(e.getCustomerName() == null ? "客户" : e.getCustomerName()).append("。");
        if (e.getContactResult() != null) sb.append("沟通结果：").append(e.getContactResult()).append("。");
        if (e.getIntention() != null) sb.append("意向").append(e.getIntention()).append("。");
        if (e.getNextAction() != null) {
            sb.append("下一步：").append(e.getNextAction());
            if (e.getNextTime() != null) sb.append("（").append(hm.format(e.getNextTime())).append("）");
            sb.append("。");
        }
        return sb.toString();
    }
}
