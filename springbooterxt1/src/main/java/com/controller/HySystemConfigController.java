package com.controller;

import com.annotation.IgnoreAuth;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.entity.HySystemConfigEntity;
import com.service.impl.HySystemConfigServiceImpl;
import com.utils.HyId;
import com.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/hyConfig")
public class HySystemConfigController {

    @Autowired
    private HySystemConfigServiceImpl service;

    /** 全部配置（品牌等），返回 name->value 映射 */
    @IgnoreAuth
    @RequestMapping("/all")
    public R all() {
        List<HySystemConfigEntity> list = service.selectList(new EntityWrapper<>());
        Map<String, String> map = new HashMap<>();
        for (HySystemConfigEntity c : list) {
            map.put(c.getName(), c.getValue());
        }
        return R.ok().put("data", map).put("list", list);
    }

    /** 按 name 取单个配置 */
    @IgnoreAuth
    @RequestMapping("/get")
    public R get(@RequestParam("name") String name) {
        HySystemConfigEntity c = service.selectOne(new EntityWrapper<HySystemConfigEntity>().eq("name", name));
        return R.ok().put("data", c == null ? null : c.getValue());
    }

    /** 新增/更新配置 */
    @PostMapping("/set")
    public R set(@RequestParam("name") String name, @RequestParam("value") String value) {
        HySystemConfigEntity c = service.selectOne(new EntityWrapper<HySystemConfigEntity>().eq("name", name));
        if (c == null) {
            c = new HySystemConfigEntity();
            c.setId(HyId.next());
            c.setName(name);
            c.setValue(value);
            service.insert(c);
        } else {
            c.setValue(value);
            service.updateById(c);
        }
        return R.ok();
    }
}
