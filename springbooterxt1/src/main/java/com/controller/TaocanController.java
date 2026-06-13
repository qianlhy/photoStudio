package com.controller;

import com.annotation.IgnoreAuth;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.entity.StoreupEntity;
import com.entity.TaocanEntity;
import com.entity.YonghuEntity;
import com.entity.view.TaocanView;
import com.service.StoreupService;
import com.service.TaocanService;
import com.service.YonghuService;
import com.utils.MPUtil;
import com.utils.PageUtils;
import com.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 套餐卡片
 * 后端接口
 */
@RestController
@RequestMapping("/taocan")
public class TaocanController {
    @Autowired
    private TaocanService taocanService;

    @Autowired
    private StoreupService storeupService;

    @Autowired
    private YonghuService yonghuService;

    /**
     * 后端列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, TaocanEntity taocan) {
        EntityWrapper<TaocanEntity> ew = new EntityWrapper<TaocanEntity>();
        PageUtils page = taocanService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, taocan), params), params));
        return R.ok().put("data", page);
    }

    /**
     * 前端列表
     */
    @IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params, TaocanEntity taocan) {
        EntityWrapper<TaocanEntity> ew = new EntityWrapper<TaocanEntity>();
        PageUtils page = taocanService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, taocan), params), params));
        return R.ok().put("data", page);
    }

    /**
     * 滑卡首页卡片流：仅上架，支持品类/风格/关键词过滤，排除当前用户已屏蔽(storeup type=22)的套餐，
     * 按排序 paixu 升序、热度 clicknum 降序输出。
     */
    @IgnoreAuth
    @RequestMapping("/cards")
    public R cards(@RequestParam Map<String, Object> params, HttpServletRequest request) {
        EntityWrapper<TaocanEntity> ew = new EntityWrapper<TaocanEntity>();
        ew.eq("shangxiajia", "上架");
        Object pinlei = params.get("pinlei");
        if (pinlei != null && !pinlei.toString().isEmpty()) {
            ew.eq("pinlei", pinlei.toString());
        }
        Object fengge = params.get("fengge");
        if (fengge != null && !fengge.toString().isEmpty()) {
            ew.eq("fengge", fengge.toString());
        }
        Object keyword = params.get("keyword");
        if (keyword != null && !keyword.toString().isEmpty()) {
            String kw = keyword.toString();
            ew.andNew().like("taocanmingcheng", kw)
                    .or().like("fengge", kw)
                    .or().like("jianjie", kw);
        }
        // 排除当前用户已屏蔽的套餐
        Object userId = request.getSession().getAttribute("userId");
        if (userId != null) {
            List<StoreupEntity> blocked = storeupService.selectList(new EntityWrapper<StoreupEntity>()
                    .eq("userid", userId).eq("tablename", "taocan").eq("type", "22"));
            List<Long> blockIds = new ArrayList<>();
            for (StoreupEntity s : blocked) {
                if (s.getRefid() != null) {
                    blockIds.add(s.getRefid());
                }
            }
            if (!blockIds.isEmpty()) {
                ew.notIn("id", blockIds);
            }
        }
        ew.orderBy("paixu", true).orderBy("clicknum", false);
        List<TaocanView> list = taocanService.selectListView(ew);
        // 登录用户按偏好风格/品类加权排序（匹配项靠前）
        if (userId != null) {
            YonghuEntity u = yonghuService.selectById((Long) userId);
            if (u != null) {
                final String prefPinlei = u.getYixiangpinlei();
                final java.util.Set<String> prefStyles = new java.util.HashSet<>();
                if (u.getPianhao() != null && !u.getPianhao().isEmpty()) {
                    for (String s : u.getPianhao().split(",")) {
                        if (s != null && !s.trim().isEmpty()) prefStyles.add(s.trim());
                    }
                }
                list.sort((a, b) -> Integer.compare(score(b, prefPinlei, prefStyles), score(a, prefPinlei, prefStyles)));
            }
        }
        return R.ok().put("data", list);
    }

    private int score(TaocanView t, String prefPinlei, java.util.Set<String> prefStyles) {
        int s = 0;
        if (prefPinlei != null && !prefPinlei.isEmpty() && !"都看看".equals(prefPinlei)) {
            if (prefPinlei.equals(t.getPinlei())) s += 10;
        }
        if (prefStyles != null && t.getFengge() != null && prefStyles.contains(t.getFengge())) s += 5;
        return s;
    }

    /**
     * 后端详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        TaocanEntity taocan = taocanService.selectById(id);
        return R.ok().put("data", taocan);
    }

    /**
     * 前端详情（同时累加热度）
     */
    @IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id) {
        TaocanEntity taocan = taocanService.selectById(id);
        if (taocan != null) {
            taocan.setClicknum((taocan.getClicknum() == null ? 0 : taocan.getClicknum()) + 1);
            taocanService.updateById(taocan);
        }
        return R.ok().put("data", taocan);
    }

    /**
     * 后端保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody TaocanEntity taocan, HttpServletRequest request) {
        taocan.setId(new Date().getTime() + new Double(Math.floor(Math.random() * 1000)).longValue());
        if (taocan.getShangxiajia() == null) {
            taocan.setShangxiajia("上架");
        }
        if (taocan.getPaixu() == null) {
            taocan.setPaixu(100);
        }
        if (taocan.getClicknum() == null) {
            taocan.setClicknum(0);
        }
        taocanService.insert(taocan);
        return R.ok();
    }

    /**
     * 前端保存
     */
    @RequestMapping("/add")
    public R add(@RequestBody TaocanEntity taocan, HttpServletRequest request) {
        taocan.setId(new Date().getTime() + new Double(Math.floor(Math.random() * 1000)).longValue());
        taocanService.insert(taocan);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody TaocanEntity taocan, HttpServletRequest request) {
        taocanService.updateById(taocan);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        taocanService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }
}
