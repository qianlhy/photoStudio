package com.controller;

import com.annotation.IgnoreAuth;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.entity.ChengpinEntity;
import com.entity.DingdanEntity;
import com.entity.MessageEntity;
import com.service.ChengpinService;
import com.service.DingdanService;
import com.service.MessageService;
import com.utils.MPUtil;
import com.utils.PageUtils;
import com.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

/**
 * 成品
 * 后端接口
 */
@RestController
@RequestMapping("/chengpin")
public class ChengpinController {
    @Autowired
    private ChengpinService chengpinService;

    @Autowired
    private DingdanService dingdanService;

    @Autowired
    private MessageService messageService;

    /**
     * 后端列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, ChengpinEntity chengpin) {
        EntityWrapper<ChengpinEntity> ew = new EntityWrapper<ChengpinEntity>();
        PageUtils page = chengpinService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, chengpin), params), params));
        return R.ok().put("data", page);
    }

    /**
     * 前端列表（仅当前用户、仅上架）
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params, ChengpinEntity chengpin, HttpServletRequest request) {
        if (!request.getSession().getAttribute("role").toString().equals("管理员")) {
            chengpin.setUserid((Long) request.getSession().getAttribute("userId"));
        }
        EntityWrapper<ChengpinEntity> ew = new EntityWrapper<ChengpinEntity>();
        PageUtils page = chengpinService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, chengpin), params), params));
        return R.ok().put("data", page);
    }

    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        return R.ok().put("data", chengpinService.selectById(id));
    }

    @IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id) {
        return R.ok().put("data", chengpinService.selectById(id));
    }

    /**
     * 后端保存：上传成品后，自动将关联订单置「成品已上线」并给客户发送消息
     */
    @RequestMapping("/save")
    public R save(@RequestBody ChengpinEntity chengpin, HttpServletRequest request) {
        chengpin.setId(new Date().getTime() + new Double(Math.floor(Math.random() * 1000)).longValue());
        if (chengpin.getShangxiajia() == null) {
            chengpin.setShangxiajia("上架");
        }
        chengpinService.insert(chengpin);
        afterPublish(chengpin);
        return R.ok();
    }

    @RequestMapping("/add")
    public R add(@RequestBody ChengpinEntity chengpin) {
        chengpin.setId(new Date().getTime() + new Double(Math.floor(Math.random() * 1000)).longValue());
        chengpinService.insert(chengpin);
        afterPublish(chengpin);
        return R.ok();
    }

    @RequestMapping("/update")
    public R update(@RequestBody ChengpinEntity chengpin) {
        chengpinService.updateById(chengpin);
        return R.ok();
    }

    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        chengpinService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }

    /**
     * 成品上线后置：更新订单状态 + 发送消息
     */
    private void afterPublish(ChengpinEntity chengpin) {
        if (!"上架".equals(chengpin.getShangxiajia())) {
            return;
        }
        if (chengpin.getDingdanid() != null) {
            DingdanEntity dingdan = dingdanService.selectById(chengpin.getDingdanid());
            if (dingdan != null) {
                dingdan.setZhuangtai("成品已上线");
                dingdanService.updateById(dingdan);
            }
        }
        if (chengpin.getUserid() != null) {
            MessageEntity msg = new MessageEntity();
            msg.setId(new Date().getTime() + new Double(Math.floor(Math.random() * 1000)).longValue());
            msg.setUserid(chengpin.getUserid());
            msg.setBiaoti("成品已上线");
            msg.setNeirong("您的订单 " + (chengpin.getDingdanbianhao() == null ? "" : chengpin.getDingdanbianhao())
                    + " 成品已上线，请前往“成品专区”在线预览或下载。");
            msg.setLeixing("成品");
            msg.setIsread("否");
            messageService.insert(msg);
        }
    }
}
