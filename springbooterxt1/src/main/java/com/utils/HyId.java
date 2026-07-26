package com.utils;

import java.util.Date;

/** 合意传媒模块统一主键生成（与 id-type=1 用户输入ID 配合） */
public class HyId {
    public static long next() {
        return new Date().getTime() + (long) Math.floor(Math.random() * 1000);
    }
}
