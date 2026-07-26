package com.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/** 对标素材短视频 */
@Data
@TableName("hy_material")
public class HyMaterialEntity implements Serializable {
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
    private String industryBig;
    private String industrySub;
    private String contentType;
    private String tags;
    private Integer likeCount;
    private Integer dislikeCount;
    private Integer favoriteCount;
    private Integer viewCount;
    private Integer usedCount;
    private Integer heat;
    private String status;
    private Integer recommend;
    private String source;
}
