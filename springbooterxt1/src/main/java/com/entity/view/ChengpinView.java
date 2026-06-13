package com.entity.view;

import com.baomidou.mybatisplus.annotations.TableName;
import com.entity.ChengpinEntity;
import org.apache.commons.beanutils.BeanUtils;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

/**
 * 成品
 * 后端返回视图实体辅助类
 */
@TableName("chengpin")
public class ChengpinView extends ChengpinEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    public ChengpinView() {
    }

    public ChengpinView(ChengpinEntity chengpinEntity) {
        try {
            BeanUtils.copyProperties(this, chengpinEntity);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
