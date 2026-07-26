package com.controller;

import com.annotation.IgnoreAuth;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.entity.HyDeliverableEntity;
import com.service.impl.HyDeliverableServiceImpl;
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
@RequestMapping("/hyDeliverable")
public class HyDeliverableController {

    @Autowired
    private HyDeliverableServiceImpl service;

    @IgnoreAuth
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HyDeliverableEntity entity) {
        EntityWrapper<HyDeliverableEntity> ew = new EntityWrapper<>();
        ew.orderBy("sort", true);
        PageUtils page = new PageUtils(service.selectPage(new Query<HyDeliverableEntity>(params).getPage(),
                MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, entity), params), params)));
        return R.ok().put("data", page);
    }

    @IgnoreAuth
    @RequestMapping("/list")
    public R list(HyDeliverableEntity entity) {
        EntityWrapper<HyDeliverableEntity> ew = new EntityWrapper<>();
        ew.orderBy("sort", true);
        return R.ok().put("data", service.selectList(MPUtil.likeOrEq(ew, entity)));
    }

    @IgnoreAuth
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        return R.ok().put("data", service.selectById(id));
    }

    @PostMapping("/save")
    public R save(@RequestBody HyDeliverableEntity entity) {
        entity.setId(HyId.next());
        entity.setAddtime(new Date());
        if (entity.getStatus() == null) entity.setStatus("上架");
        service.insert(entity);
        return R.ok().put("id", entity.getId());
    }

    @RequestMapping("/update")
    public R update(@RequestBody HyDeliverableEntity entity) {
        service.updateById(entity);
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
        service.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }
}
