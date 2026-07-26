package com.controller;

import com.annotation.IgnoreAuth;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.entity.HyFollowTaskEntity;
import com.service.impl.HyFollowTaskServiceImpl;
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
@RequestMapping("/hyFollowTask")
public class HyFollowTaskController {

    @Autowired
    private HyFollowTaskServiceImpl service;

    @IgnoreAuth
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HyFollowTaskEntity entity) {
        EntityWrapper<HyFollowTaskEntity> ew = new EntityWrapper<>();
        PageUtils page = new PageUtils(service.selectPage(new Query<HyFollowTaskEntity>(params).getPage(),
                MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, entity), params), params)));
        return R.ok().put("data", page);
    }

    @IgnoreAuth
    @RequestMapping("/list")
    public R list(HyFollowTaskEntity entity) {
        EntityWrapper<HyFollowTaskEntity> ew = new EntityWrapper<>();
        return R.ok().put("data", service.selectList(MPUtil.likeOrEq(ew, entity)));
    }

    @IgnoreAuth
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        return R.ok().put("data", service.selectById(id));
    }

    @PostMapping("/save")
    public R save(@RequestBody HyFollowTaskEntity entity) {
        entity.setId(HyId.next());
        entity.setAddtime(new Date());
        if (entity.getStatus() == null) entity.setStatus("待办");
        service.insert(entity);
        return R.ok().put("id", entity.getId());
    }

    @RequestMapping("/update")
    public R update(@RequestBody HyFollowTaskEntity entity) {
        service.updateById(entity);
        return R.ok();
    }

    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        service.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }
}
