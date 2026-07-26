package com.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.dao.HyMessageDao;
import com.entity.HyMessageEntity;
import org.springframework.stereotype.Service;

@Service("hyMessageServiceImpl")
public class HyMessageServiceImpl extends ServiceImpl<HyMessageDao, HyMessageEntity> {
}
