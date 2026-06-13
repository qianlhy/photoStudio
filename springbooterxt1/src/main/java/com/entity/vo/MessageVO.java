package com.entity.vo;

import java.io.Serializable;

/**
 * 系统消息
 * 手机端接口返回实体辅助类
 */
public class MessageVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userid;
    private String biaoti;
    private String neirong;
    private String leixing;
    private String isread;

    public Long getUserid() {
        return userid;
    }

    public void setUserid(Long userid) {
        this.userid = userid;
    }

    public String getBiaoti() {
        return biaoti;
    }

    public void setBiaoti(String biaoti) {
        this.biaoti = biaoti;
    }

    public String getNeirong() {
        return neirong;
    }

    public void setNeirong(String neirong) {
        this.neirong = neirong;
    }

    public String getLeixing() {
        return leixing;
    }

    public void setLeixing(String leixing) {
        this.leixing = leixing;
    }

    public String getIsread() {
        return isread;
    }

    public void setIsread(String isread) {
        this.isread = isread;
    }
}
