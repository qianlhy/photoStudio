package com.controller;

import com.annotation.IgnoreAuth;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.entity.HyContentPlanEntity;
import com.service.impl.HyContentPlanServiceImpl;
import com.service.impl.HyDealServiceImpl;
import com.utils.HyId;
import com.utils.MPUtil;
import com.utils.PageUtils;
import com.utils.Query;
import com.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/hyContentPlan")
public class HyContentPlanController {

    @Autowired
    private HyContentPlanServiceImpl service;
    @Autowired
    private HyDealServiceImpl dealService;

    @IgnoreAuth
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HyContentPlanEntity entity) {
        EntityWrapper<HyContentPlanEntity> ew = new EntityWrapper<>();
        ew.orderBy("addtime", false);
        PageUtils page = new PageUtils(service.selectPage(new Query<HyContentPlanEntity>(params).getPage(),
                MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, entity), params), params)));
        return R.ok().put("data", page);
    }

    @IgnoreAuth
    @RequestMapping("/list")
    public R list(HyContentPlanEntity entity) {
        EntityWrapper<HyContentPlanEntity> ew = new EntityWrapper<>();
        return R.ok().put("data", service.selectList(MPUtil.likeOrEq(ew, entity)));
    }

    @IgnoreAuth
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        return R.ok().put("data", service.selectById(id));
    }

    /** 结束选片生成方案后，自动将客户置为待付款并创建行动事项 */
    @PostMapping("/save")
    public R save(@RequestBody HyContentPlanEntity entity) {
        entity.setId(HyId.next());
        entity.setAddtime(new Date());
        if (entity.getConfirmed() == null) entity.setConfirmed(0);
        service.insert(entity);
        dealService.markAwaitingPayment(entity.getCustomerId(), entity.getId());
        return R.ok().put("id", entity.getId());
    }

    @RequestMapping("/update")
    public R update(@RequestBody HyContentPlanEntity entity) {
        service.updateById(entity);
        return R.ok();
    }

    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        service.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }
}
