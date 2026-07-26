package com.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.dao.HyCustomerDao;
import com.entity.HyCustomerEntity;
import org.springframework.stereotype.Service;

@Service("hyCustomerServiceImpl")
public class HyCustomerServiceImpl extends ServiceImpl<HyCustomerDao, HyCustomerEntity> {
}
