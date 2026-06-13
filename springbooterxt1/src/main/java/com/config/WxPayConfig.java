package com.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信支付配置（对应 application.yml 中 wxpay.*）。
 * enabled=false 或凭证未填写时，下单走“到店缴费锁档”线下流程。
 */
@Component
@ConfigurationProperties(prefix = "wxpay")
public class WxPayConfig {

    private boolean enabled;
    private String appid;
    private String mchid;
    private String apiV3Key;
    private String certSerialNo;
    private String privateKeyPath;
    private String notifyUrl;

    /** 是否已具备真实下单条件（开关打开且凭证非占位值） */
    public boolean isConfigured() {
        return enabled
                && notPlaceholder(mchid)
                && notPlaceholder(apiV3Key)
                && notPlaceholder(certSerialNo)
                && notPlaceholder(appid);
    }

    private boolean notPlaceholder(String v) {
        return v != null && !v.trim().isEmpty() && !v.startsWith("your_");
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getMchid() {
        return mchid;
    }

    public void setMchid(String mchid) {
        this.mchid = mchid;
    }

    public String getApiV3Key() {
        return apiV3Key;
    }

    public void setApiV3Key(String apiV3Key) {
        this.apiV3Key = apiV3Key;
    }

    public String getCertSerialNo() {
        return certSerialNo;
    }

    public void setCertSerialNo(String certSerialNo) {
        this.certSerialNo = certSerialNo;
    }

    public String getPrivateKeyPath() {
        return privateKeyPath;
    }

    public void setPrivateKeyPath(String privateKeyPath) {
        this.privateKeyPath = privateKeyPath;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }
}
