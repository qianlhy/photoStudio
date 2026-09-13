package com.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/** 客户档案 */
@Data
@TableName("hy_customer")
public class HyCustomerEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date addtime;

    private String name;
    private String contact;
    private String phone;
    private String industry;
    private String biztype;
    private String scale;
    private Long managerId;
    private String managerName;
    private String followStatus;
    private String intention;
    private String tags;
    private String preference;
    private String avatar;

    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date lastFollowTime;

    private Integer dealCount;
    private Integer unpaidCount;
    private BigDecimal unpaidAmount;
    private Integer satisfaction;
    private Integer selectedCount;
    private Integer shotCount;
    private Integer deliveredCount;
    private Integer publishedCount;
    private Integer remainCount;
    private Integer publishDays;
    private String publishDeadline;

    /** 选片目标条数（后台按客户配置，Pad 选片读取） */
    private Integer selectTarget;

    /** 小程序登录审核：待审核 / 已通过 / 已驳回 */
    private String auditStatus;
    /** 审核回复（驳回原因等） */
    private String auditReply;
    /** 微信 openid（小程序登录） */
    private String openid;
    /** 意向品类：写真/宣传片/都看看 */
    private String yixiangPinlei;
}
