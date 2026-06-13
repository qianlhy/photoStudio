package com.entity.vo;

import java.io.Serializable;

/**
 * 成品
 * 手机端接口返回实体辅助类
 */
public class ChengpinVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long dingdanid;
    private String dingdanbianhao;
    private String taocanmingcheng;
    private Long userid;
    private String xingming;
    private String biaoti;
    private String tupian;
    private String shipin;
    private String shangxiajia;
    private String beizhu;

    public Long getDingdanid() {
        return dingdanid;
    }

    public void setDingdanid(Long dingdanid) {
        this.dingdanid = dingdanid;
    }

    public String getDingdanbianhao() {
        return dingdanbianhao;
    }

    public void setDingdanbianhao(String dingdanbianhao) {
        this.dingdanbianhao = dingdanbianhao;
    }

    public String getTaocanmingcheng() {
        return taocanmingcheng;
    }

    public void setTaocanmingcheng(String taocanmingcheng) {
        this.taocanmingcheng = taocanmingcheng;
    }

    public Long getUserid() {
        return userid;
    }

    public void setUserid(Long userid) {
        this.userid = userid;
    }

    public String getXingming() {
        return xingming;
    }

    public void setXingming(String xingming) {
        this.xingming = xingming;
    }

    public String getBiaoti() {
        return biaoti;
    }

    public void setBiaoti(String biaoti) {
        this.biaoti = biaoti;
    }

    public String getTupian() {
        return tupian;
    }

    public void setTupian(String tupian) {
        this.tupian = tupian;
    }

    public String getShipin() {
        return shipin;
    }

    public void setShipin(String shipin) {
        this.shipin = shipin;
    }

    public String getShangxiajia() {
        return shangxiajia;
    }

    public void setShangxiajia(String shangxiajia) {
        this.shangxiajia = shangxiajia;
    }

    public String getBeizhu() {
        return beizhu;
    }

    public void setBeizhu(String beizhu) {
        this.beizhu = beizhu;
    }
}
