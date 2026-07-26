package com.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/** 行动事项（销售行动中心） */
@Data
@TableName("hy_action_item")
public class HyActionItemEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date addtime;

    /** 待付款/客诉/制作预警/库存不足 */
    private String type;
    private Long customerId;
    private String customerName;
    private Long orderId;
    private String title;
    private Long ownerId;
    private String ownerName;
    private String intention;
    private Integer innerProgress;
    private Integer canIntervene;
    private String status;
    private String belong;
}
