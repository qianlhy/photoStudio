package com.controller;

import com.annotation.IgnoreAuth;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.entity.HyActionItemEntity;
import com.service.impl.HyActionItemServiceImpl;
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
@RequestMapping("/hyActionItem")
public class HyActionItemController {

    @Autowired
    private HyActionItemServiceImpl service;
    @Autowired
    private HyDealServiceImpl dealService;

    @IgnoreAuth
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HyActionItemEntity entity) {
        EntityWrapper<HyActionItemEntity> ew = new EntityWrapper<>();
        ew.orderBy("addtime", false);
        PageUtils page = new PageUtils(service.selectPage(new Query<HyActionItemEntity>(params).getPage(),
                MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, entity), params), params)));
        return R.ok().put("data", page);
    }

    @IgnoreAuth
    @RequestMapping("/list")
    public R list(HyActionItemEntity entity) {
        EntityWrapper<HyActionItemEntity> ew = new EntityWrapper<>();
        ew.orderBy("addtime", false);
        return R.ok().put("data", service.selectList(MPUtil.likeOrEq(ew, entity)));
    }

    @IgnoreAuth
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        return R.ok().put("data", service.selectById(id));
    }

    /** 刷新制作预警：内部周期 50% 关注，2/3 且落后才开放销售干预 */
    @IgnoreAuth
    @RequestMapping("/refreshProgress")
    public R refreshProgress() {
        int n = dealService.refreshProductionWarnings();
        return R.ok().put("updated", n);
    }

    @PostMapping("/save")
    public R save(@RequestBody HyActionItemEntity entity) {
        entity.setId(HyId.next());
        entity.setAddtime(new Date());
        if (entity.getStatus() == null) entity.setStatus("待处理");
        service.insert(entity);
        return R.ok().put("id", entity.getId());
    }

    @RequestMapping("/update")
    public R update(@RequestBody HyActionItemEntity entity) {
        service.updateById(entity);
        return R.ok();
    }

    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        service.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }
}
