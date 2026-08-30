-- 客户档案统一小程序审核状态（在客户管理里审核，登录时以 hy_customer 为准）
ALTER TABLE `hy_customer`
  ADD COLUMN `audit_status` varchar(20) DEFAULT '已通过' COMMENT '小程序审核:待审核/已通过/已驳回' AFTER `publish_deadline`,
  ADD COLUMN `audit_reply` varchar(500) DEFAULT NULL COMMENT '审核回复' AFTER `audit_status`;

-- 历史客户默认已通过（后台创建的客户可直接登录）
UPDATE `hy_customer` SET `audit_status` = '已通过' WHERE `audit_status` IS NULL OR `audit_status` = '';

-- 已有业务经理或已成交的客户，强制视为已通过
UPDATE `hy_customer`
SET `audit_status` = '已通过'
WHERE `manager_id` IS NOT NULL
   OR IFNULL(`deal_count`, 0) > 0
   OR `follow_status` = '已成交';

-- 同步 yonghu 可登录
UPDATE `yonghu` y
INNER JOIN `hy_customer` c ON c.`phone` = y.`shoujihaoma`
SET y.`sfsh` = '是', y.`shhf` = IFNULL(c.`audit_reply`, '')
WHERE c.`audit_status` = '已通过';
