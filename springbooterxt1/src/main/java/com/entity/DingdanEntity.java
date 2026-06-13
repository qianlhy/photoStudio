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
 * 订单
 * 数据库通用操作实体类（普通增删改查）
 */
@TableName("dingdan")
public class DingdanEntity<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    public DingdanEntity() {
    }

    public DingdanEntity(T t) {
        try {
            BeanUtils.copyProperties(this, t);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /** 主键id */
    @TableId
    private Long id;
    /** 订单编号 */
    private String dingdanbianhao;
    /** 套餐id */
    private Long taocanid;
    /** 套餐名称(快照) */
    private String taocanmingcheng;
    /** 套餐封面(快照) */
    private String fengmian;
    /** 品类(快照) */
    private String pinlei;
    /** 风格(快照) */
    private String fengge;
    /** 线下标价(快照) */
    private Double xianxiabiaojia;
    /** 用户id */
    private Long userid;
    /** 账号 */
    private String zhanghao;
    /** 姓名 */
    private String xingming;
    /** 手机号码 */
    private String shoujihaoma;
    /** 拍摄人数 */
    private Integer paisherenshu;
    /** 意向拍摄档期 */
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat
    private Date yixiangdangqi;
    /** 商家预估档期 */
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat
    private Date yugudangqi;
    /** 个性化拍摄备注 */
    private String beizhu;
    /** 排队序号 */
    private Integer paiduixuhao;
    /** 订单状态 */
    private String zhuangtai;
    /** 是否缴费锁定档期(是/否) */
    private String jiaofeisuoding;
    /** 取消原因 */
    private String quxiaoyuanyin;

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
