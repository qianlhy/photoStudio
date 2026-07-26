package com.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.dao.HyOrderDao;
import com.entity.HyOrderEntity;
import org.springframework.stereotype.Service;

@Service("hyOrderServiceImpl")
public class HyOrderServiceImpl extends ServiceImpl<HyOrderDao, HyOrderEntity> {
}
