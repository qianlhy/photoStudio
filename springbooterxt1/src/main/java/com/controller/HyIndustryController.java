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

import javax.servlet.http.HttpServletRequest;
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

    @Autowired
    private com.service.impl.HyOperationLogServiceImpl operationLogService;

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
    public R save(@RequestBody HyIndustryEntity entity, HttpServletRequest request) {
        if (entity.getName() == null || entity.getName().trim().isEmpty()) {
            return R.error("分类名称不能为空");
        }
        entity.setName(entity.getName().trim());
        if (entity.getLevel() == null) {
            entity.setLevel(entity.getParentId() != null && entity.getParentId() > 0 ? 2 : 1);
        }
        if (entity.getLevel() == 2) {
            if (entity.getParentId() == null || entity.getParentId() <= 0) {
                return R.error("请选择所属行业大类");
            }
            HyIndustryEntity parent = service.selectById(entity.getParentId());
            if (parent == null) return R.error("所属行业不存在");
            entity.setParentName(parent.getName());
        } else {
            entity.setParentId(0L);
            entity.setParentName(null);
            entity.setLevel(1);
        }
        EntityWrapper<HyIndustryEntity> dup = new EntityWrapper<>();
        dup.eq("name", entity.getName()).eq("level", entity.getLevel());
        if (entity.getLevel() == 2) {
            dup.eq("parent_id", entity.getParentId());
        } else {
            dup.eq("parent_id", 0);
        }
        if (service.selectCount(dup) > 0) {
            return R.error("同级分类名称已存在");
        }
        entity.setId(HyId.next());
        entity.setAddtime(new Date());
        if (entity.getSort() == null) {
            entity.setSort(nextSort(entity.getLevel(), entity.getParentId()));
        }
        if (entity.getHeat() == null) entity.setHeat(50);
        service.insert(entity);
        operationLogService.record(request, "素材内容", "新增分类",
                (entity.getLevel() == 2 ? entity.getParentName() + " > " : "") + entity.getName());
        return R.ok().put("id", entity.getId());
    }

    @RequestMapping("/update")
    public R update(@RequestBody HyIndustryEntity entity, HttpServletRequest request) {
        if (entity.getId() == null) return R.error("分类 id 必填");
        HyIndustryEntity old = service.selectById(entity.getId());
        if (old == null) return R.error("分类不存在");
        if (entity.getName() != null) entity.setName(entity.getName().trim());
        if (entity.getName() == null || entity.getName().isEmpty()) {
            return R.error("分类名称不能为空");
        }
        EntityWrapper<HyIndustryEntity> dup = new EntityWrapper<>();
        dup.eq("name", entity.getName()).eq("level", old.getLevel()).ne("id", old.getId());
        if (old.getLevel() != null && old.getLevel() == 2) {
            Long pid = entity.getParentId() != null ? entity.getParentId() : old.getParentId();
            dup.eq("parent_id", pid);
        } else {
            dup.eq("parent_id", 0);
        }
        if (service.selectCount(dup) > 0) {
            return R.error("同级分类名称已存在");
        }
        String oldName = old.getName();
        String newName = entity.getName();
        if (old.getLevel() != null && old.getLevel() == 1) {
            old.setName(newName);
            if (entity.getSort() != null) old.setSort(entity.getSort());
            if (entity.getCover() != null) old.setCover(entity.getCover());
            service.updateById(old);
            if (!oldName.equals(newName)) {
                cascadeBigRename(oldName, newName);
            }
        } else {
            old.setName(newName);
            if (entity.getParentId() != null && entity.getParentId() > 0) {
                HyIndustryEntity parent = service.selectById(entity.getParentId());
                if (parent == null) return R.error("所属行业不存在");
                old.setParentId(parent.getId());
                old.setParentName(parent.getName());
            }
            if (entity.getSort() != null) old.setSort(entity.getSort());
            if (entity.getCover() != null) old.setCover(entity.getCover());
            service.updateById(old);
            if (!oldName.equals(newName)) {
                cascadeSubRename(old.getParentName(), oldName, newName);
            }
        }
        operationLogService.record(request, "素材内容", "修改分类",
                (old.getLevel() == 2 ? old.getParentName() + " > " : "") + newName);
        return R.ok();
    }

    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids, HttpServletRequest request) {
        if (ids == null || ids.length == 0) return R.error("请选择要删除的分类");
        for (Long id : ids) {
            HyIndustryEntity ind = service.selectById(id);
            if (ind == null) continue;
            if (ind.getLevel() != null && ind.getLevel() == 1) {
                int children = service.selectCount(new EntityWrapper<HyIndustryEntity>().eq("parent_id", ind.getId()));
                if (children > 0) {
                    return R.error("「" + ind.getName() + "」下仍有业态分类，请先删除子分类");
                }
                int mats = materialService.selectCount(new EntityWrapper<HyMaterialEntity>()
                        .eq("industry_big", ind.getName()).ne("status", "回收站"));
                if (mats > 0) {
                    return R.error("「" + ind.getName() + "」下仍有 " + mats + " 条素材，无法删除");
                }
            } else {
                int mats = materialService.selectCount(new EntityWrapper<HyMaterialEntity>()
                        .eq("industry_sub", ind.getName())
                        .eq("industry_big", ind.getParentName())
                        .ne("status", "回收站"));
                if (mats > 0) {
                    return R.error("「" + ind.getParentName() + " > " + ind.getName() + "」下仍有 "
                            + mats + " 条素材，无法删除");
                }
            }
        }
        service.deleteBatchIds(Arrays.asList(ids));
        operationLogService.record(request, "素材内容", "删除分类", "ID：" + Arrays.toString(ids));
        return R.ok();
    }

    private int nextSort(Integer level, Long parentId) {
        EntityWrapper<HyIndustryEntity> ew = new EntityWrapper<>();
        ew.eq("level", level == null ? 1 : level);
        if (level != null && level == 2 && parentId != null) {
            ew.eq("parent_id", parentId);
        } else {
            ew.eq("parent_id", 0);
        }
        ew.orderBy("sort", false).last("limit 1");
        List<HyIndustryEntity> list = service.selectList(ew);
        if (list == null || list.isEmpty() || list.get(0).getSort() == null) return 1;
        return list.get(0).getSort() + 1;
    }

    /** 行业大类改名：同步素材与小类 parent_name */
    private void cascadeBigRename(String oldName, String newName) {
        List<HyMaterialEntity> mats = materialService.selectList(
                new EntityWrapper<HyMaterialEntity>().eq("industry_big", oldName));
        for (HyMaterialEntity m : mats) {
            m.setIndustryBig(newName);
            materialService.updateById(m);
        }
        List<HyIndustryEntity> subs = service.selectList(
                new EntityWrapper<HyIndustryEntity>().eq("parent_name", oldName));
        for (HyIndustryEntity s : subs) {
            s.setParentName(newName);
            service.updateById(s);
        }
    }

    /** 业态改名：同步素材 industry_sub */
    private void cascadeSubRename(String parentName, String oldName, String newName) {
        EntityWrapper<HyMaterialEntity> ew = new EntityWrapper<HyMaterialEntity>();
        ew.eq("industry_sub", oldName);
        if (parentName != null && !parentName.isEmpty()) {
            ew.eq("industry_big", parentName);
        }
        List<HyMaterialEntity> mats = materialService.selectList(ew);
        for (HyMaterialEntity m : mats) {
            m.setIndustrySub(newName);
            materialService.updateById(m);
        }
    }
}
