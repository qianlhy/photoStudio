package com.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.dao.HyOperationLogDao;
import com.entity.HyOperationLogEntity;
import org.springframework.stereotype.Service;

@Service("hyOperationLogServiceImpl")
public class HyOperationLogServiceImpl extends ServiceImpl<HyOperationLogDao, HyOperationLogEntity> {
}
