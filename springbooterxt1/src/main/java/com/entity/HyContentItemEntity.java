package com.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/** 订单内容清单 */
@Data
@TableName("hy_content_item")
public class HyContentItemEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date addtime;

    private Long orderId;
    private String title;
    private String contentType;
    private Long materialRef;
    private String cover;
    private Integer status;
    private Integer sort;
}
