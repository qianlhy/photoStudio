package com.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/** 内容方案(五类配方) */
@Data
@TableName("hy_content_plan")
public class HyContentPlanEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date addtime;

    private Long customerId;
    private String customerName;
    private Long sessionId;
    private String originalSelection;
    @JsonAlias({"rProcess", "rprocess"})
    private Integer rProcess;
    @JsonAlias({"rKnowledge", "rknowledge"})
    private Integer rKnowledge;
    @JsonAlias({"rStory", "rstory"})
    private Integer rStory;
    @JsonAlias({"rOpinion", "ropinion"})
    private Integer rOpinion;
    @JsonAlias({"rAd", "rad"})
    private Integer rAd;
    private Integer totalCount;
    private String finalMaterials;
    private Integer confirmed;
}
