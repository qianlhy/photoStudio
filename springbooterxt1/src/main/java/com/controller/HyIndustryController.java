package com.controller;

import com.annotation.IgnoreAuth;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.entity.HyIndustryEntity;
import com.entity.HyMaterialEntity;
import com.service.impl.HyIndustryServiceImpl;
import com.service.impl.HyMaterialServiceImpl;
import com.utils.HyId;
import com.utils.MPUtil;
import com.utils.PageUtils;
import com.utils.Query;
import com.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/hyIndustry")
public class HyIndustryController {

    @Autowired
    private HyIndustryServiceImpl service;

    @Autowired
    private HyMaterialServiceImpl materialService;

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
        List<HyIndustryEntity> list = service.selectList(MPUtil.likeOrEq(ew, entity));
        fillMaterialCounts(list);
        return R.ok().put("data", list);
    }

    /** 按已上架素材实时统计各行业条数，避免使用种子数据里的假数字 */
    private void fillMaterialCounts(List<HyIndustryEntity> list) {
        List<HyMaterialEntity> materials = materialService.selectList(
                new EntityWrapper<HyMaterialEntity>().eq("status", "上架"));
        Map<String, Integer> sub = new HashMap<>();
        Map<String, Integer> big = new HashMap<>();
        for (HyMaterialEntity m : materials) {
            if (m.getIndustrySub() != null) {
                sub.put(m.getIndustrySub(), sub.getOrDefault(m.getIndustrySub(), 0) + 1);
            }
            if (m.getIndustryBig() != null) {
                big.put(m.getIndustryBig(), big.getOrDefault(m.getIndustryBig(), 0) + 1);
            }
        }
        for (HyIndustryEntity ind : list) {
            if (ind.getLevel() != null && ind.getLevel() == 1) {
                ind.setMaterialCount(big.getOrDefault(ind.getName(), 0));
            } else if (ind.getLevel() != null && ind.getLevel() == 2) {
                ind.setMaterialCount(sub.getOrDefault(ind.getName(), 0));
            } else {
                ind.setMaterialCount(0);
            }
        }
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
