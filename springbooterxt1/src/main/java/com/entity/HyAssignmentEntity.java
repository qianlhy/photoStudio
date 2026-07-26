package com.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/** 客户交接记录 */
@Data
@TableName("hy_assignment")
public class HyAssignmentEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date addtime;

    private Long customerId;
    private String customerName;
    private Long fromManagerId;
    private String fromManagerName;
    private Long toManagerId;
    private String toManagerName;
    private String remark;
    private String operator;
}
