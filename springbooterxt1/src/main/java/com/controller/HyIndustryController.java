package com.controller;

import com.annotation.IgnoreAuth;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.entity.HyIndustryEntity;
import com.service.impl.HyIndustryServiceImpl;
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
@RequestMapping("/hyIndustry")
public class HyIndustryController {

    @Autowired
    private HyIndustryServiceImpl service;

    @IgnoreAuth
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HyIndustryEntity entity) {
        EntityWrapper<HyIndustryEntity> ew = new EntityWrapper<>();
        PageUtils page = new PageUtils(service.selectPage(new Query<HyIndustryEntity>(params).getPage(),
                MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, entity), params), params)));
        return R.ok().put("data", page);
    }

    @IgnoreAuth
    @RequestMapping("/list")
    public R list(HyIndustryEntity entity) {
        EntityWrapper<HyIndustryEntity> ew = new EntityWrapper<>();
        ew.orderBy("sort", true);
        return R.ok().put("data", service.selectList(MPUtil.likeOrEq(ew, entity)));
    }

    @IgnoreAuth
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        return R.ok().put("data", service.selectById(id));
    }

    @PostMapping("/save")
    public R save(@RequestBody HyIndustryEntity entity) {
        entity.setId(HyId.next());
        entity.setAddtime(new Date());
        service.insert(entity);
        return R.ok().put("id", entity.getId());
    }

    @RequestMapping("/update")
    public R update(@RequestBody HyIndustryEntity entity) {
        service.updateById(entity);
        return R.ok();
    }

    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        service.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }
}
