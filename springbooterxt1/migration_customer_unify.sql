-- 客户主数据整合：业务字段以 hy_customer 为准，yonghu 仅作登录 token 兼容壳
-- 可重复执行：字段已存在时请跳过对应 ALTER

ALTER TABLE `hy_customer`
  ADD COLUMN `openid` varchar(100) DEFAULT NULL COMMENT '微信openid' AFTER `audit_reply`,
  ADD COLUMN `yixiang_pinlei` varchar(50) DEFAULT NULL COMMENT '意向品类:写真/宣传片/都看看' AFTER `openid`;

-- 历史数据：从 yonghu 灌入客户主表（主表为空才覆盖）
UPDATE `hy_customer` c
INNER JOIN `yonghu` y ON y.`shoujihaoma` = c.`phone`
SET
  c.`preference` = CASE
      WHEN (c.`preference` IS NULL OR c.`preference` = '') AND y.`pianhao` IS NOT NULL AND y.`pianhao` <> ''
        THEN y.`pianhao` ELSE c.`preference` END,
  c.`openid` = CASE
      WHEN (c.`openid` IS NULL OR c.`openid` = '') AND y.`openid` IS NOT NULL AND y.`openid` <> ''
        THEN y.`openid` ELSE c.`openid` END,
  c.`yixiang_pinlei` = CASE
      WHEN (c.`yixiang_pinlei` IS NULL OR c.`yixiang_pinlei` = '') AND y.`yixiangpinlei` IS NOT NULL AND y.`yixiangpinlei` <> ''
        THEN y.`yixiangpinlei` ELSE c.`yixiang_pinlei` END,
  c.`audit_status` = CASE
      WHEN y.`sfsh` = '是' THEN '已通过'
      WHEN y.`sfsh` = '驳回' AND (c.`manager_id` IS NULL AND IFNULL(c.`deal_count`,0)=0 AND IFNULL(c.`follow_status`,'')<>'已成交')
        THEN '已驳回'
      WHEN y.`sfsh` = '否' AND (c.`manager_id` IS NULL AND IFNULL(c.`deal_count`,0)=0 AND IFNULL(c.`follow_status`,'')<>'已成交')
           AND (c.`audit_status` IS NULL OR c.`audit_status` = '' OR c.`audit_status` = '待审核')
        THEN '待审核'
      ELSE IFNULL(NULLIF(c.`audit_status`,''), '已通过') END;

-- 已建档 CRM 客户强制已通过
UPDATE `hy_customer`
SET `audit_status` = '已通过'
WHERE `manager_id` IS NOT NULL
   OR IFNULL(`deal_count`, 0) > 0
   OR `follow_status` = '已成交';

-- 反向：主表偏好/openid 回写 yonghu，保证旧接口可读
UPDATE `yonghu` y
INNER JOIN `hy_customer` c ON c.`phone` = y.`shoujihaoma`
SET
  y.`pianhao` = IFNULL(NULLIF(c.`preference`, ''), y.`pianhao`),
  y.`yixiangpinlei` = IFNULL(NULLIF(c.`yixiang_pinlei`, ''), y.`yixiangpinlei`),
  y.`openid` = IFNULL(NULLIF(c.`openid`, ''), y.`openid`),
  y.`sfsh` = CASE WHEN c.`audit_status` = '已通过' THEN '是'
                  WHEN c.`audit_status` = '已驳回' THEN '驳回'
                  WHEN c.`audit_status` = '待审核' THEN '否'
                  ELSE y.`sfsh` END,
  y.`xingming` = IFNULL(NULLIF(c.`name`, ''), y.`xingming`);
