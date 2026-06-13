package com.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;

/**
 * 套餐卡片
 * 数据库通用操作实体类（普通增删改查）
 */
@TableName("taocan")
public class TaocanEntity<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    public TaocanEntity() {
    }

    public TaocanEntity(T t) {
        try {
            BeanUtils.copyProperties(this, t);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /** 主键id */
    @TableId
    private Long id;
    /** 套餐名称 */
    private String taocanmingcheng;
    /** 品类(写真/宣传片) */
    private String pinlei;
    /** 风格 */
    private String fengge;
    /** 样片封面(多图,逗号分隔) */
    private String fengmian;
    /** 样片短片 */
    private String shipin;
    /** 套餐简介 */
    private String jianjie;
    /** 线下标价 */
    private Double xianxiabiaojia;
    /** 服装数量 */
    private Integer fuzhuangshuliang;
    /** 精修张数 */
    private Integer jingxiuzhangshu;
    /** 拍摄时长 */
    private String paishishichang;
    /** 附加升级项目 */
    private String shengjixiangmu;
    /** 实拍成片(多图) */
    private String shctp;
    /** 上下架状态 */
    private String shangxiajia;
    /** 排序 */
    private Integer paixu;
    /** 热度/点击量 */
    private Integer clicknum;

    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date addtime;

    public Date getAddtime() {
        return addtime;
    }

    public void setAddtime(Date addtime) {
        this.addtime = addtime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
