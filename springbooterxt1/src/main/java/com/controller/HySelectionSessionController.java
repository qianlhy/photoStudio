package com.controller;

import com.annotation.IgnoreAuth;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.entity.HySelectionSessionEntity;
import com.service.impl.HySelectionSessionServiceImpl;
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
@RequestMapping("/hySelectionSession")
public class HySelectionSessionController {

    @Autowired
    private HySelectionSessionServiceImpl service;

    @IgnoreAuth
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HySelectionSessionEntity entity) {
        EntityWrapper<HySelectionSessionEntity> ew = new EntityWrapper<>();
        ew.orderBy("addtime", false);
        PageUtils page = new PageUtils(service.selectPage(new Query<HySelectionSessionEntity>(params).getPage(),
                MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, entity), params), params)));
        return R.ok().put("data", page);
    }

    @IgnoreAuth
    @RequestMapping("/list")
    public R list(HySelectionSessionEntity entity) {
        EntityWrapper<HySelectionSessionEntity> ew = new EntityWrapper<>();
        return R.ok().put("data", service.selectList(MPUtil.likeOrEq(ew, entity)));
    }

    @IgnoreAuth
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        return R.ok().put("data", service.selectById(id));
    }

    @PostMapping("/save")
    public R save(@RequestBody HySelectionSessionEntity entity) {
        entity.setId(HyId.next());
        entity.setAddtime(new Date());
        if (entity.getStatus() == null) entity.setStatus("进行中");
        service.insert(entity);
        return R.ok().put("id", entity.getId());
    }

    @RequestMapping("/update")
    public R update(@RequestBody HySelectionSessionEntity entity) {
        service.updateById(entity);
        return R.ok();
    }

    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        service.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }
}
