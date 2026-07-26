package com.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.dao.HyEmployeeDao;
import com.entity.HyEmployeeEntity;
import org.springframework.stereotype.Service;

@Service("hyEmployeeServiceImpl")
public class HyEmployeeServiceImpl extends ServiceImpl<HyEmployeeDao, HyEmployeeEntity> {
}
