package com.controller;

import com.annotation.IgnoreAuth;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.entity.DingdanEntity;
import com.entity.TaocanEntity;
import com.service.DingdanService;
import com.service.TaocanService;
import com.utils.MPUtil;
import com.utils.OrderStatus;
import com.utils.PageUtils;
import com.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

/**
 * 订单
 * 后端接口
 */
@RestController
@RequestMapping("/dingdan")
public class DingdanController {

    /** 下单串行锁：保证“算排队号 + 落库 + 提交”整体串行，避免并发重号 */
    private static final Object ORDER_LOCK = new Object();

    @Autowired
    private DingdanService dingdanService;

    @Autowired
    private TaocanService taocanService;

    /**
     * 后端列表（商家可看全部，普通用户只看自己）
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, DingdanEntity dingdan, HttpServletRequest request) {
        if (!request.getSession().getAttribute("role").toString().equals("管理员")) {
            dingdan.setUserid((Long) request.getSession().getAttribute("userId"));
        }
        EntityWrapper<DingdanEntity> ew = new EntityWrapper<DingdanEntity>();
        PageUtils page = dingdanService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, dingdan), params), params));
        return R.ok().put("data", page);
    }

    /**
     * 前端列表（我的订单）
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params, DingdanEntity dingdan, HttpServletRequest request) {
        if (!request.getSession().getAttribute("role").toString().equals("管理员")) {
            dingdan.setUserid((Long) request.getSession().getAttribute("userId"));
        }
        EntityWrapper<DingdanEntity> ew = new EntityWrapper<DingdanEntity>();
        PageUtils page = dingdanService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, dingdan), params), params));
        return R.ok().put("data", page);
    }

    /**
     * 后端详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        return R.ok().put("data", dingdanService.selectById(id));
    }

    /**
     * 前端详情
     */
    @IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id) {
        return R.ok().put("data", dingdanService.selectById(id));
    }

    /**
     * 下单：仅锁档期，付款线下完成。自动生成订单编号、排队序号，状态置「待排队」。
     */
    @RequestMapping("/add")
    public R add(@RequestBody DingdanEntity dingdan, HttpServletRequest request) {
        Long uId = (Long) request.getSession().getAttribute("userId");
        dingdan.setId(new Date().getTime() + new Double(Math.floor(Math.random() * 1000)).longValue());
        dingdan.setUserid(uId);
        // 套餐快照
        if (dingdan.getTaocanid() != null) {
            TaocanEntity tc = taocanService.selectById(dingdan.getTaocanid());
            if (tc != null) {
                dingdan.setTaocanmingcheng(tc.getTaocanmingcheng());
                dingdan.setFengmian(tc.getFengmian());
                dingdan.setPinlei(tc.getPinlei());
                dingdan.setFengge(tc.getFengge());
                dingdan.setXianxiabiaojia(tc.getXianxiabiaojia());
            }
        }
        dingdan.setDingdanbianhao("DD" + new Date().getTime() + (int) (Math.random() * 900 + 100));
        dingdan.setZhuangtai(OrderStatus.PENDING_QUEUE);
        dingdan.setJiaofeisuoding("否");
        // 串行化“算排队号 + 落库 + 提交”，防止并发下重复排队号
        synchronized (ORDER_LOCK) {
            dingdanService.createOrder(dingdan);
        }
        return R.ok().put("dingdanbianhao", dingdan.getDingdanbianhao()).put("paiduixuhao", dingdan.getPaiduixuhao());
    }

    /**
     * 后端保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody DingdanEntity dingdan, HttpServletRequest request) {
        dingdan.setId(new Date().getTime() + new Double(Math.floor(Math.random() * 1000)).longValue());
        if (dingdan.getZhuangtai() == null) {
            dingdan.setZhuangtai("待排队");
        }
        if (dingdan.getDingdanbianhao() == null) {
            dingdan.setDingdanbianhao("DD" + new Date().getTime() + (int) (Math.random() * 900 + 100));
        }
        dingdanService.insert(dingdan);
        return R.ok();
    }

    /**
     * 修改（含状态推进、改期、调序、收款锁档、取消等通用更新）
     */
    @RequestMapping("/update")
    public R update(@RequestBody DingdanEntity dingdan, HttpServletRequest request) {
        dingdanService.updateChecked(dingdan);
        return R.ok();
    }

    /**
     * 用户取消订单（带取消原因，受状态机约束）
     */
    @RequestMapping("/cancel")
    public R cancel(@RequestParam Long id, @RequestParam(required = false) String reason, HttpServletRequest request) {
        dingdanService.cancelOrder(id, reason);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        dingdanService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }
}
