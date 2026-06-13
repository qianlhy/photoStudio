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
 * 类型
 * 数据库通用操作实体类（普通增删改查）
 *
 * @author
 * @email

 */
@TableName("leixing")
public class LeixingEntity<T> implements Serializable {
    private static final long serialVersionUID = 1L;


    public LeixingEntity() {

    }

    public LeixingEntity(T t) {
        try {
            BeanUtils.copyProperties(this, t);
        } catch (IllegalAccessException | InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 主键id
     */
    @TableId
    private Long id;
    /**
     * 类型
     */

    private String leixing;

    /**
     * 所属品类(写真/宣传片)
     */
    private String pinlei;


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

    /**
     * 设置：类型
     */
    public void setLeixing(String leixing) {
        this.leixing = leixing;
    }

    /**
     * 获取：类型
     */
    public String getLeixing() {
        return leixing;
    }

    public String getPinlei() {
        return pinlei;
    }

    public void setPinlei(String pinlei) {
        this.pinlei = pinlei;
    }

}
