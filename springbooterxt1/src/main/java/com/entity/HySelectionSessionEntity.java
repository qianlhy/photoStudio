package com.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/** 选片会话 */
@Data
@TableName("hy_selection_session")
public class HySelectionSessionEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date addtime;

    private Long customerId;
    private String customerName;
    private Long managerId;
    private String managerName;
    private String industry;
    private String biztype;
    private Integer targetCount;
    private Integer selectedCount;
    private String liked;
    private String disliked;
    private Integer cProcess;
    private Integer cKnowledge;
    private Integer cStory;
    private Integer cOpinion;
    private Integer cAd;
    private String status;
}
