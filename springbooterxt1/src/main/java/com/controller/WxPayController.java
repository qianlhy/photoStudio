package com.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.annotation.IgnoreAuth;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.config.WxPayConfig;
import com.entity.DingdanEntity;
import com.entity.YonghuEntity;
import com.service.DingdanService;
import com.service.YonghuService;
import com.utils.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 微信小程序支付（JSAPI / 微信支付 v3）骨架。
 *
 * 当前为代码骨架：已打通“下单 -> 取 openid -> 组装统一下单参数”与“回调入口 -> 幂等更新订单”，
 * 其中“RSA 签名调用微信下单接口”“回调验签 + 资源解密”两处依赖商户证书/APIv3 密钥，
 * 待 wxpay.* 凭证填写齐全（enabled=true）后在 TODO 处补全即可启用。
 *
 * 凭证未配置时，下单接口返回友好提示，前端继续走“到店缴费锁定档期”线下流程。
 */
@RestController
@RequestMapping("/wxpay")
public class WxPayController {

    private static final Logger log = LoggerFactory.getLogger(WxPayController.class);

    @Autowired
    private WxPayConfig wxPayConfig;

    @Autowired
    private DingdanService dingdanService;

    @Autowired
    private YonghuService yonghuService;

    @Autowired
    private com.service.impl.HyCustomerAccountServiceImpl customerAccountService;

    /**
     * 统一下单（JSAPI），返回小程序 wx.requestPayment 所需参数。
     */
    @RequestMapping("/prepay")
    public R prepay(@RequestParam String dingdanbianhao, HttpServletRequest request) {
        if (!wxPayConfig.isConfigured()) {
            return R.error("微信支付未配置，当前为到店缴费锁档模式");
        }

        DingdanEntity order = dingdanService.selectOne(new EntityWrapper<DingdanEntity>()
                .eq("dingdanbianhao", dingdanbianhao));
        if (order == null) {
            return R.error("订单不存在");
        }
        if ("已缴费锁定档期".equals(order.getJiaofeisuoding())) {
            return R.error("该订单已完成缴费");
        }

        Long uid = (Long) request.getSession().getAttribute("userId");
        YonghuEntity user = uid == null ? null : yonghuService.selectById(uid);
        if (user != null) {
            customerAccountService.enrichYonghuFromCustomer(user);
        }
        String openid = user == null ? null : user.getOpenid();
        if (openid == null || openid.isEmpty()) {
            return R.error("缺少 openid，请先使用微信登录后再支付");
        }

        // 金额（分）：以套餐线下标价为准
        long totalFee = toFen(order.getXianxiabiaojia());

        // 组装微信支付 v3 JSAPI 统一下单请求体
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("appid", wxPayConfig.getAppid());
        body.put("mchid", wxPayConfig.getMchid());
        body.put("description", "照相馆预约定金-" + safe(order.getTaocanmingcheng()));
        body.put("out_trade_no", order.getDingdanbianhao());
        body.put("notify_url", wxPayConfig.getNotifyUrl());
        Map<String, Object> amount = new HashMap<>();
        amount.put("total", totalFee);
        amount.put("currency", "CNY");
        body.put("amount", amount);
        Map<String, Object> payer = new HashMap<>();
        payer.put("openid", openid);
        body.put("payer", payer);

        String requestJson = JSON.toJSONString(body);
        log.info("微信统一下单请求体: {}", requestJson);

        // TODO(支付凭证就绪后实现)：
        //  1) 用商户私钥(privateKeyPath)对 (POST,/v3/pay/transactions/jsapi,timestamp,nonce,body) 做 RSA-SHA256 签名
        //  2) 设置 Authorization: WECHATPAY2-SHA256-RSA2048 ... 头，POST 到
        //     https://api.mch.weixin.qq.com/v3/pay/transactions/jsapi
        //  3) 取返回的 prepay_id，按 wx.requestPayment 规则再次签名，返回
        //     { appId, timeStamp, nonceStr, package: "prepay_id=...", signType: "RSA", paySign }
        return R.error("微信支付下单待接入：请补全商户私钥/证书序列号后实现 v3 签名调用");
    }

    /**
     * 微信支付结果回调。微信服务器调用，无需登录态。
     */
    @IgnoreAuth
    @PostMapping("/notify")
    public Map<String, String> notify(HttpServletRequest request) {
        Map<String, String> ack = new HashMap<>();
        try {
            String bodyStr = readBody(request);
            log.info("收到微信支付回调: {}", bodyStr);

            // TODO(支付凭证就绪后实现)：
            //  1) 校验 Wechatpay-Signature / Wechatpay-Timestamp / Wechatpay-Nonce / Wechatpay-Serial（平台证书验签）
            //  2) 用 APIv3 密钥对 resource(AES-256-GCM) 解密，得到明文 JSON
            //  3) 校验 trade_state == SUCCESS，取 out_trade_no
            //  4) 调 dingdanService.markPaid(out_trade_no) 幂等更新订单
            // 在凭证就绪前，这里不做任何订单变更，避免出现未验签的伪造回调改单。

            if (!wxPayConfig.isConfigured()) {
                ack.put("code", "FAIL");
                ack.put("message", "支付未启用");
                return ack;
            }

            JSONObject json = JSON.parseObject(bodyStr);
            // 占位：实际应来自解密后的 resource 明文
            String outTradeNo = json == null ? null : json.getString("out_trade_no");
            if (outTradeNo != null) {
                boolean changed = dingdanService.markPaid(outTradeNo);
                log.info("订单 {} 缴费回调处理完成，是否变更={}", outTradeNo, changed);
            }
            ack.put("code", "SUCCESS");
            ack.put("message", "成功");
        } catch (Exception e) {
            log.error("处理微信支付回调失败", e);
            ack.put("code", "FAIL");
            ack.put("message", "处理失败");
        }
        return ack;
    }

    private long toFen(Double yuan) {
        if (yuan == null) {
            return 0L;
        }
        return Math.round(yuan * 100);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String readBody(HttpServletRequest request) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }
}
