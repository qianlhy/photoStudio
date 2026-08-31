-- 修复微信登录 500：给 hy_customer 补齐审核/openid 字段（可重复执行）
-- 在线上库 ssm48yhg 执行

-- 1) 审核字段（若已存在会报 Duplicate column，可忽略该句）
ALTER TABLE `hy_customer`
  ADD COLUMN `audit_status` varchar(20) DEFAULT '已通过' COMMENT '小程序审核:待审核/已通过/已驳回' AFTER `publish_deadline`;

ALTER TABLE `hy_customer`
  ADD COLUMN `audit_reply` varchar(500) DEFAULT NULL COMMENT '审核回复' AFTER `audit_status`;

-- 2) openid / 意向品类（微信登录必需）
ALTER TABLE `hy_customer`
  ADD COLUMN `openid` varchar(100) DEFAULT NULL COMMENT '微信openid' AFTER `audit_reply`;

ALTER TABLE `hy_customer`
  ADD COLUMN `yixiang_pinlei` varchar(50) DEFAULT NULL COMMENT '意向品类:写真/宣传片/都看看' AFTER `openid`;

-- 3) 默认把已有客户标为已通过，避免历史数据无法登录
UPDATE `hy_customer`
SET `audit_status` = '已通过'
WHERE `audit_status` IS NULL OR `audit_status` = '';

-- 4) 从 yonghu 回填 openid（有则写）
UPDATE `hy_customer` c
INNER JOIN `yonghu` y ON (
  y.`shoujihaoma` = c.`phone` OR y.`zhanghao` = c.`phone`
)
SET c.`openid` = CASE
      WHEN (c.`openid` IS NULL OR c.`openid` = '') AND y.`openid` IS NOT NULL AND y.`openid` <> ''
        THEN y.`openid` ELSE c.`openid` END;
