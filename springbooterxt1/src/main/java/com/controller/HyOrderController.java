package com.controller;

import com.annotation.IgnoreAuth;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.entity.HyContentItemEntity;
import com.entity.HyOrderEntity;
import com.service.impl.HyContentItemServiceImpl;
import com.service.impl.HyOrderServiceImpl;
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
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/hyOrder")
public class HyOrderController {

    @Autowired
    private HyOrderServiceImpl service;
    @Autowired
    private HyContentItemServiceImpl itemService;

    @IgnoreAuth
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HyOrderEntity entity) {
        EntityWrapper<HyOrderEntity> ew = new EntityWrapper<>();
        ew.orderBy("addtime", false);
        PageUtils page = new PageUtils(service.selectPage(new Query<HyOrderEntity>(params).getPage(),
                MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, entity), params), params)));
        return R.ok().put("data", page);
    }

    @IgnoreAuth
    @RequestMapping("/list")
    public R list(HyOrderEntity entity) {
        EntityWrapper<HyOrderEntity> ew = new EntityWrapper<>();
        ew.orderBy("addtime", false);
        return R.ok().put("data", service.selectList(MPUtil.likeOrEq(ew, entity)));
    }

    @IgnoreAuth
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        return R.ok().put("data", service.selectById(id));
    }

    /** 订单详情 + 本次内容清单 */
    @IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id) {
        HyOrderEntity order = service.selectById(id);
        List<HyContentItemEntity> items = itemService.selectList(
                new EntityWrapper<HyContentItemEntity>().eq("order_id", id).orderBy("sort", true));
        return R.ok().put("data", order).put("items", items);
    }

    @PostMapping("/save")
    public R save(@RequestBody HyOrderEntity entity) {
        entity.setId(HyId.next());
        entity.setAddtime(new Date());
        if (entity.getOrderNo() == null || entity.getOrderNo().isEmpty()) {
            entity.setOrderNo("YJ-" + new SimpleDateFormat("MMdd").format(new Date()) + "-"
                    + (int) (Math.random() * 900 + 100));
        }
        if (entity.getStatus() == null) entity.setStatus("待拍摄");
        service.insert(entity);
        return R.ok().put("id", entity.getId());
    }

    @RequestMapping("/update")
    public R update(@RequestBody HyOrderEntity entity) {
        service.updateById(entity);
        return R.ok();
    }

    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        service.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }
}
