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
 * 成品
 * 数据库通用操作实体类（普通增删改查）
 */
@TableName("chengpin")
public class ChengpinEntity<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    public ChengpinEntity() {
    }

    public ChengpinEntity(T t) {
        try {
            BeanUtils.copyProperties(this, t);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @TableId
    private Long id;
    /** 订单id */
    private Long dingdanid;
    /** 订单编号(快照) */
    private String dingdanbianhao;
    /** 套餐名称(快照) */
    private String taocanmingcheng;
    /** 接收用户id */
    private Long userid;
    /** 客户姓名(快照) */
    private String xingming;
    /** 成品标题 */
    private String biaoti;
    /** 成品图片(多图) */
    private String tupian;
    /** 成品视频 */
    private String shipin;
    /** 上下架状态 */
    private String shangxiajia;
    /** 备注 */
    private String beizhu;

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
