package com.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/** 订单 */
@Data
@TableName("hy_order")
public class HyOrderEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date addtime;

    private String orderNo;
    private Long customerId;
    private String customerName;
    private String packageName;
    private Integer videoCount;
    private Integer completedCount;
    private Long managerId;
    private String managerName;
    private Long shooterId;
    private String shooterName;
    private Long editorId;
    private String editorName;
    private String industry;
    private String biztype;

    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date shootDate;

    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date targetDate;

    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date deliverDate;

    private String status;
    private String recipe;
    private String abnormal;
    private Integer qualityFlag;
    private BigDecimal amount;
}
