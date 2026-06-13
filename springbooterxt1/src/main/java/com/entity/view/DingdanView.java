package com.entity.view;

import com.baomidou.mybatisplus.annotations.TableName;
import com.entity.DingdanEntity;
import org.apache.commons.beanutils.BeanUtils;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

/**
 * 订单
 * 后端返回视图实体辅助类
 */
@TableName("dingdan")
public class DingdanView extends DingdanEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    public DingdanView() {
    }

    public DingdanView(DingdanEntity dingdanEntity) {
        try {
            BeanUtils.copyProperties(this, dingdanEntity);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
