package com.entity.view;

import com.baomidou.mybatisplus.annotations.TableName;
import com.entity.TaocanEntity;
import org.apache.commons.beanutils.BeanUtils;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

/**
 * 套餐卡片
 * 后端返回视图实体辅助类
 */
@TableName("taocan")
public class TaocanView extends TaocanEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    public TaocanView() {
    }

    public TaocanView(TaocanEntity taocanEntity) {
        try {
            BeanUtils.copyProperties(this, taocanEntity);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
