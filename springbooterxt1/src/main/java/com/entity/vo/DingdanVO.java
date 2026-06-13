package com.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 订单
 * 手机端接口返回实体辅助类
 */
public class DingdanVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String dingdanbianhao;
    private Long taocanid;
    private String taocanmingcheng;
    private String fengmian;
    private String pinlei;
    private String fengge;
    private Double xianxiabiaojia;
    private Long userid;
    private String zhanghao;
    private String xingming;
    private String shoujihaoma;
    private Integer paisherenshu;
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat
    private Date yixiangdangqi;
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat
    private Date yugudangqi;
    private String beizhu;
    private Integer paiduixuhao;
    private String zhuangtai;
    private String jiaofeisuoding;
    private String quxiaoyuanyin;

    public String getDingdanbianhao() {
        return dingdanbianhao;
    }

    public void setDingdanbianhao(String dingdanbianhao) {
        this.dingdanbianhao = dingdanbianhao;
    }

    public Long getTaocanid() {
        return taocanid;
    }

    public void setTaocanid(Long taocanid) {
        this.taocanid = taocanid;
    }

    public String getTaocanmingcheng() {
        return taocanmingcheng;
    }

    public void setTaocanmingcheng(String taocanmingcheng) {
        this.taocanmingcheng = taocanmingcheng;
    }

    public String getFengmian() {
        return fengmian;
    }

    public void setFengmian(String fengmian) {
        this.fengmian = fengmian;
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

    public Double getXianxiabiaojia() {
        return xianxiabiaojia;
    }

    public void setXianxiabiaojia(Double xianxiabiaojia) {
        this.xianxiabiaojia = xianxiabiaojia;
    }

    public Long getUserid() {
        return userid;
    }

    public void setUserid(Long userid) {
        this.userid = userid;
    }

    public String getZhanghao() {
        return zhanghao;
    }

    public void setZhanghao(String zhanghao) {
        this.zhanghao = zhanghao;
    }

    public String getXingming() {
        return xingming;
    }

    public void setXingming(String xingming) {
        this.xingming = xingming;
    }

    public String getShoujihaoma() {
        return shoujihaoma;
    }

    public void setShoujihaoma(String shoujihaoma) {
        this.shoujihaoma = shoujihaoma;
    }

    public Integer getPaisherenshu() {
        return paisherenshu;
    }

    public void setPaisherenshu(Integer paisherenshu) {
        this.paisherenshu = paisherenshu;
    }

    public Date getYixiangdangqi() {
        return yixiangdangqi;
    }

    public void setYixiangdangqi(Date yixiangdangqi) {
        this.yixiangdangqi = yixiangdangqi;
    }

    public Date getYugudangqi() {
        return yugudangqi;
    }

    public void setYugudangqi(Date yugudangqi) {
        this.yugudangqi = yugudangqi;
    }

    public String getBeizhu() {
        return beizhu;
    }

    public void setBeizhu(String beizhu) {
        this.beizhu = beizhu;
    }

    public Integer getPaiduixuhao() {
        return paiduixuhao;
    }

    public void setPaiduixuhao(Integer paiduixuhao) {
        this.paiduixuhao = paiduixuhao;
    }

    public String getZhuangtai() {
        return zhuangtai;
    }

    public void setZhuangtai(String zhuangtai) {
        this.zhuangtai = zhuangtai;
    }

    public String getJiaofeisuoding() {
        return jiaofeisuoding;
    }

    public void setJiaofeisuoding(String jiaofeisuoding) {
        this.jiaofeisuoding = jiaofeisuoding;
    }

    public String getQuxiaoyuanyin() {
        return quxiaoyuanyin;
    }

    public void setQuxiaoyuanyin(String quxiaoyuanyin) {
        this.quxiaoyuanyin = quxiaoyuanyin;
    }
}
