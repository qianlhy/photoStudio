package com.entity.model;

import java.io.Serializable;

/**
 * 套餐卡片
 * 接收传参的实体类
 */
public class TaocanModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private String taocanmingcheng;
    private String pinlei;
    private String fengge;
    private String fengmian;
    private String shipin;
    private String jianjie;
    private Double xianxiabiaojia;
    private Integer fuzhuangshuliang;
    private Integer jingxiuzhangshu;
    private String paishishichang;
    private String shengjixiangmu;
    private String shctp;
    private String shangxiajia;
    private Integer paixu;
    private Integer clicknum;

    public String getTaocanmingcheng() {
        return taocanmingcheng;
    }

    public void setTaocanmingcheng(String taocanmingcheng) {
        this.taocanmingcheng = taocanmingcheng;
    }

    public String getPinlei() {
        return pinlei;
    }

    public void setPinlei(String pinlei) {
        this.pinlei = pinlei;
    }

    public String getFengge() {
        return fengge;
    }

    public void setFengge(String fengge) {
        this.fengge = fengge;
    }

    public String getFengmian() {
        return fengmian;
    }

    public void setFengmian(String fengmian) {
        this.fengmian = fengmian;
    }

    public String getShipin() {
        return shipin;
    }

    public void setShipin(String shipin) {
        this.shipin = shipin;
    }

    public String getJianjie() {
        return jianjie;
    }

    public void setJianjie(String jianjie) {
        this.jianjie = jianjie;
    }

    public Double getXianxiabiaojia() {
        return xianxiabiaojia;
    }

    public void setXianxiabiaojia(Double xianxiabiaojia) {
        this.xianxiabiaojia = xianxiabiaojia;
    }

    public Integer getFuzhuangshuliang() {
        return fuzhuangshuliang;
    }

    public void setFuzhuangshuliang(Integer fuzhuangshuliang) {
        this.fuzhuangshuliang = fuzhuangshuliang;
    }

    public Integer getJingxiuzhangshu() {
        return jingxiuzhangshu;
    }

    public void setJingxiuzhangshu(Integer jingxiuzhangshu) {
        this.jingxiuzhangshu = jingxiuzhangshu;
    }

    public String getPaishishichang() {
        return paishishichang;
    }

    public void setPaishishichang(String paishishichang) {
        this.paishishichang = paishishichang;
    }

    public String getShengjixiangmu() {
        return shengjixiangmu;
    }

    public void setShengjixiangmu(String shengjixiangmu) {
        this.shengjixiangmu = shengjixiangmu;
    }

    public String getShctp() {
        return shctp;
    }

    public void setShctp(String shctp) {
        this.shctp = shctp;
    }

    public String getShangxiajia() {
        return shangxiajia;
    }

    public void setShangxiajia(String shangxiajia) {
        this.shangxiajia = shangxiajia;
    }

    public Integer getPaixu() {
        return paixu;
    }

    public void setPaixu(Integer paixu) {
        this.paixu = paixu;
    }

    public Integer getClicknum() {
        return clicknum;
    }

    public void setClicknum(Integer clicknum) {
        this.clicknum = clicknum;
    }
}
