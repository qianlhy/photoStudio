package com.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 员工账号（合意传媒，三端统一身份）
 */
@Data
@TableName("hy_employee")
public class HyEmployeeEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date addtime;

    private String username;
    private String password;
    private String name;
    private String avatar;
    private String phone;
    /** 管理员/销售经理/拍摄/剪辑 */
    private String role;
    private String department;
    /** 正常/停用 */
    private String status;
    private String permissions;
    private Integer customerCount;
    private Integer taskCount;

    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date lastlogin;
}
