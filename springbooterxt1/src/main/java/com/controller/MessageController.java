package com.controller;

import com.annotation.IgnoreAuth;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.entity.MessageEntity;
import com.entity.YonghuEntity;
import com.service.MessageNotifyService;
import com.service.MessageService;
import com.service.YonghuService;
import com.utils.MPUtil;
import com.utils.PageUtils;
import com.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
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

    @Autowired
    private MessageNotifyService messageNotifyService;

    @Autowired
    private YonghuService yonghuService;

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

    /**
     * 用户提交售后/改期等留言（写入消息中心）
     */
    @RequestMapping("/feedback")
    public R feedback(@RequestParam String content,
                      @RequestParam(required = false) String orderNo,
                      @RequestParam(defaultValue = "售后") String leixing,
                      HttpServletRequest request) {
        Long userId = (Long) request.getSession().getAttribute("userId");
        if (userId == null) return R.error("请先登录");
        if (content == null || content.trim().isEmpty()) return R.error("请填写留言内容");
        MessageEntity msg = new MessageEntity();
        msg.setId(new Date().getTime() + (long) (Math.random() * 1000));
        msg.setUserid(userId);
        msg.setBiaoti(leixing + "申请");
        msg.setNeirong((orderNo != null ? "订单 " + orderNo + "：" : "") + content.trim());
        msg.setLeixing(leixing);
        msg.setIsread("否");
        messageService.insert(msg);
        messageNotifyService.sendFeedback(userId, orderNo, content.trim());
        return R.ok("已提交，客服将尽快处理");
    }

    /**
     * 商家群发活动/系统消息
     */
    @RequestMapping("/push")
    public R push(@RequestParam String title,
                  @RequestParam String content,
                  @RequestParam(required = false) Long userid,
                  @RequestParam(defaultValue = "活动") String leixing) {
        if (title == null || title.trim().isEmpty()) return R.error("请填写标题");
        if (userid != null) {
            messageNotifyService.sendActivity(userid, title, content);
        } else {
            List<YonghuEntity> users = yonghuService.selectList(
                    new EntityWrapper<YonghuEntity>().eq("sfsh", "是"));
            for (YonghuEntity u : users) {
                messageNotifyService.sendActivity(u.getId(), title, content);
            }
        }
        return R.ok("推送成功");
    }
}
