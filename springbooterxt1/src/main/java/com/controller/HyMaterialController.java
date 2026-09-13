package com.controller;

import com.annotation.IgnoreAuth;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.entity.HyMaterialEntity;
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
import java.util.Map;

@RestController
@RequestMapping("/hyMaterial")
public class HyMaterialController {

    @Autowired
    private HyMaterialServiceImpl service;
    @Autowired
    private com.service.impl.HyOperationLogServiceImpl operationLogService;

    @IgnoreAuth
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HyMaterialEntity entity) {
        EntityWrapper<HyMaterialEntity> ew = new EntityWrapper<>();
        ew.orderBy("heat", false);
        PageUtils page = new PageUtils(service.selectPage(new Query<HyMaterialEntity>(params).getPage(),
                MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, entity), params), params)));
        return R.ok().put("data", page);
    }

    @IgnoreAuth
    @RequestMapping("/list")
    public R list(HyMaterialEntity entity) {
        EntityWrapper<HyMaterialEntity> ew = new EntityWrapper<>();
        ew.orderBy("heat", false);
        return R.ok().put("data", service.selectList(MPUtil.likeOrEq(ew, entity)));
    }

    @IgnoreAuth
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        return R.ok().put("data", service.selectById(id));
    }

    @PostMapping("/save")
    public R save(@RequestBody HyMaterialEntity entity, HttpServletRequest request) {
        entity.setId(HyId.next());
        entity.setAddtime(new Date());
        if (entity.getStatus() == null) entity.setStatus("上架");
        service.insert(entity);
        operationLogService.record(request, "素材内容", "新增素材",
                entity.getTitle() != null ? entity.getTitle() : String.valueOf(entity.getId()));
        return R.ok().put("id", entity.getId());
    }

    /** 批量上传保存 */
    @PostMapping("/batchSave")
    public R batchSave(@RequestBody java.util.List<HyMaterialEntity> list, HttpServletRequest request) {
        int n = 0;
        if (list != null) {
            for (HyMaterialEntity e : list) {
                e.setId(HyId.next());
                e.setAddtime(new Date());
                if (e.getStatus() == null) e.setStatus("上架");
                service.insert(e);
                n++;
            }
        }
        operationLogService.record(request, "素材内容", "批量上传", "共 " + n + " 条素材");
        return R.ok();
    }

    @RequestMapping("/update")
    public R update(@RequestBody HyMaterialEntity entity, HttpServletRequest request) {
        service.updateById(entity);
        operationLogService.record(request, "素材内容", "修改素材",
                (entity.getTitle() != null ? entity.getTitle() : "") + " ID:" + entity.getId()
                        + (entity.getStatus() != null ? " / " + entity.getStatus() : ""));
        return R.ok();
    }

    /** 选片：喜欢 */
    @IgnoreAuth
    @RequestMapping("/like/{id}")
    public R like(@PathVariable("id") Long id) {
        HyMaterialEntity e = service.selectById(id);
        if (e == null) return R.error("素材不存在");
        e.setLikeCount((e.getLikeCount() == null ? 0 : e.getLikeCount()) + 1);
        e.setUsedCount((e.getUsedCount() == null ? 0 : e.getUsedCount()) + 1);
        e.setHeat((e.getHeat() == null ? 0 : e.getHeat()) + 2);
        service.updateById(e);
        return R.ok();
    }

    /** 选片：不喜欢 */
    @IgnoreAuth
    @RequestMapping("/dislike/{id}")
    public R dislike(@PathVariable("id") Long id) {
        HyMaterialEntity e = service.selectById(id);
        if (e == null) return R.error("素材不存在");
        e.setDislikeCount((e.getDislikeCount() == null ? 0 : e.getDislikeCount()) + 1);
        service.updateById(e);
        return R.ok();
    }

    /** 收藏 */
    @IgnoreAuth
    @RequestMapping("/favorite/{id}")
    public R favorite(@PathVariable("id") Long id) {
        HyMaterialEntity e = service.selectById(id);
        if (e == null) return R.error("素材不存在");
        e.setFavoriteCount((e.getFavoriteCount() == null ? 0 : e.getFavoriteCount()) + 1);
        service.updateById(e);
        return R.ok();
    }

    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids, HttpServletRequest request) {
        service.deleteBatchIds(Arrays.asList(ids));
        operationLogService.record(request, "素材内容", "删除素材",
                "素材ID：" + (ids == null ? "" : Arrays.toString(ids)));
        return R.ok();
    }
}
