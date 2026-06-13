package com.controller;

import com.annotation.IgnoreAuth;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.entity.MessageEntity;
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
 * 系统消息
 * 后端接口
 */
@RestController
@RequestMapping("/message")
public class MessageController {
    @Autowired
    private MessageService messageService;

    /**
     * 后端列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, MessageEntity message) {
        EntityWrapper<MessageEntity> ew = new EntityWrapper<MessageEntity>();
        PageUtils page = messageService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, message), params), params));
        return R.ok().put("data", page);
    }

    /**
     * 前端列表（仅当前用户）
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params, MessageEntity message, HttpServletRequest request) {
        if (!request.getSession().getAttribute("role").toString().equals("管理员")) {
            message.setUserid((Long) request.getSession().getAttribute("userId"));
        }
        EntityWrapper<MessageEntity> ew = new EntityWrapper<MessageEntity>();
        PageUtils page = messageService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, message), params), params));
        return R.ok().put("data", page);
    }

    /**
     * 未读数量
     */
    @RequestMapping("/unread")
    public R unread(HttpServletRequest request) {
        Long userId = (Long) request.getSession().getAttribute("userId");
        int count = messageService.selectCount(new EntityWrapper<MessageEntity>()
                .eq("userid", userId).eq("isread", "否"));
        return R.ok().put("count", count);
    }

    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        return R.ok().put("data", messageService.selectById(id));
    }

    @IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id) {
        return R.ok().put("data", messageService.selectById(id));
    }

    @RequestMapping("/save")
    public R save(@RequestBody MessageEntity message) {
        message.setId(new Date().getTime() + new Double(Math.floor(Math.random() * 1000)).longValue());
        if (message.getIsread() == null) {
            message.setIsread("否");
        }
        messageService.insert(message);
        return R.ok();
    }

    @RequestMapping("/add")
    public R add(@RequestBody MessageEntity message) {
        message.setId(new Date().getTime() + new Double(Math.floor(Math.random() * 1000)).longValue());
        messageService.insert(message);
        return R.ok();
    }

    @RequestMapping("/update")
    public R update(@RequestBody MessageEntity message) {
        messageService.updateById(message);
        return R.ok();
    }

    /**
     * 标记已读
     */
    @RequestMapping("/read/{id}")
    public R read(@PathVariable("id") Long id) {
        MessageEntity message = messageService.selectById(id);
        if (message != null) {
            message.setIsread("是");
            messageService.updateById(message);
        }
        return R.ok();
    }

    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        messageService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }
}
