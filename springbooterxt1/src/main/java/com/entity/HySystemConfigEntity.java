package com.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;

import java.io.Serializable;

/** 系统配置（品牌/全局参数） */
@Data
@TableName("hy_system_config")
public class HySystemConfigEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    private String name;
    private String value;
}
