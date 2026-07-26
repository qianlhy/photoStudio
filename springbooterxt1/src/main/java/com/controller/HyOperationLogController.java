package com.controller;

import com.annotation.IgnoreAuth;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.entity.HyOperationLogEntity;
import com.service.impl.HyOperationLogServiceImpl;
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
@RequestMapping("/hyOperationLog")
public class HyOperationLogController {

    @Autowired
    private HyOperationLogServiceImpl service;

    @IgnoreAuth
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HyOperationLogEntity entity) {
        EntityWrapper<HyOperationLogEntity> ew = new EntityWrapper<>();
        ew.orderBy("addtime", false);
        PageUtils page = new PageUtils(service.selectPage(new Query<HyOperationLogEntity>(params).getPage(),
                MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, entity), params), params)));
        return R.ok().put("data", page);
    }

    @IgnoreAuth
    @RequestMapping("/list")
    public R list(HyOperationLogEntity entity) {
        EntityWrapper<HyOperationLogEntity> ew = new EntityWrapper<>();
        ew.orderBy("addtime", false);
        return R.ok().put("data", service.selectList(MPUtil.likeOrEq(ew, entity)));
    }

    @PostMapping("/save")
    public R save(@RequestBody HyOperationLogEntity entity) {
        entity.setId(HyId.next());
        entity.setAddtime(new Date());
        service.insert(entity);
        return R.ok();
    }

    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        service.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }
}
