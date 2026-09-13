package com.entity;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/** 操作日志（后台员工与系统） */
@Data
@TableName("hy_operation_log")
public class HyOperationLogEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date addtime;

    /** 操作人展示名（列 operator） */
    @TableField("operator")
    private String operatorName;

    /** 模块：客户管理/订单管理/素材内容/成品/员工与系统 等 */
    private String module;

    /** 动作：新增/修改/审核/划拨/删除… */
    private String action;

    /** 详情说明 */
    private String detail;
}
