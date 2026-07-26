package com.controller;

import com.annotation.IgnoreAuth;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.entity.HyMessageEntity;
import com.service.impl.HyMessageServiceImpl;
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
@RequestMapping("/hyMessage")
public class HyMessageController {

    @Autowired
    private HyMessageServiceImpl service;

    @IgnoreAuth
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HyMessageEntity entity) {
        EntityWrapper<HyMessageEntity> ew = new EntityWrapper<>();
        ew.orderBy("addtime", false);
        PageUtils page = new PageUtils(service.selectPage(new Query<HyMessageEntity>(params).getPage(),
                MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, entity), params), params)));
        return R.ok().put("data", page);
    }

    @IgnoreAuth
    @RequestMapping("/list")
    public R list(HyMessageEntity entity) {
        EntityWrapper<HyMessageEntity> ew = new EntityWrapper<>();
        ew.orderBy("addtime", false);
        return R.ok().put("data", service.selectList(MPUtil.likeOrEq(ew, entity)));
    }

    @IgnoreAuth
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        return R.ok().put("data", service.selectById(id));
    }

    /** 客户/系统提交消息：按关键字自动分流销售/编导 */
    @IgnoreAuth
    @PostMapping("/save")
    public R save(@RequestBody HyMessageEntity entity) {
        entity.setId(HyId.next());
        entity.setAddtime(new Date());
        if (entity.getBelong() == null) {
            entity.setBelong(routeBelong(entity.getTitle(), entity.getContent()));
        }
        if (entity.getStatus() == null) {
            entity.setStatus("编导".equals(entity.getBelong()) ? "已自动分配编导" : "待处理");
        }
        service.insert(entity);
        return R.ok().put("id", entity.getId()).put("belong", entity.getBelong());
    }

    @RequestMapping("/update")
    public R update(@RequestBody HyMessageEntity entity) {
        service.updateById(entity);
        return R.ok();
    }

    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        service.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }

    /** 简单规则分流：涉及制作/拍摄/剪辑/时间 → 编导；其余 → 销售 */
    private String routeBelong(String title, String content) {
        String t = ((title == null ? "" : title) + (content == null ? "" : content));
        String[] editorKeys = {"拍摄", "剪辑", "制作", "时间", "改", "进度", "脚本", "镜头", "交付"};
        for (String k : editorKeys) {
            if (t.contains(k)) return "编导";
        }
        return "销售";
    }
}
