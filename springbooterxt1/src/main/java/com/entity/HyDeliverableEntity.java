package com.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/** 成品与优质作品 */
@Data
@TableName("hy_deliverable")
public class HyDeliverableEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date addtime;

    private String title;
    private String cover;
    private String video;
    private Integer duration;
    private Long orderId;
    private String orderNo;
    private Long customerId;
    private String customerName;
    private String industry;
    private String contentType;
    private String shooterName;
    private String editorName;
    private String viewStatus;
    private String downloadStatus;

    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date downloadTime;

    private Integer satisfaction;
    private String customerComment;
    private Integer qualityFlag;
    private String reuseValue;
    private String applyIndustry;
    private String status;
    private Integer sort;
}
