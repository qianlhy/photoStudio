-- ============================================================
-- 合意传媒 本轮上线迁移（先执行本文件，再部署 JAR / 后台）
-- 说明：某列已存在时对应 ADD 会报错，跳过该句继续即可
-- ============================================================

-- 1) 客户选片目标条数（后台可配置，Pad 选片读取）
ALTER TABLE `hy_customer`
  ADD COLUMN `select_target` int DEFAULT 15 COMMENT '选片目标条数(后台配置,Pad读取)' AFTER `publish_deadline`;

UPDATE `hy_customer` SET `select_target` = 15 WHERE `select_target` IS NULL;

-- 2) 操作日志字段对齐（模块 / 详情）
ALTER TABLE `hy_operation_log`
  ADD COLUMN `module` varchar(100) DEFAULT NULL COMMENT '模块' AFTER `operator`;

ALTER TABLE `hy_operation_log`
  ADD COLUMN `detail` varchar(500) DEFAULT NULL COMMENT '详情' AFTER `action`;

-- 旧字段 target → detail（无 target 列可跳过整段 UPDATE）
UPDATE `hy_operation_log`
SET `detail` = `target`
WHERE (`detail` IS NULL OR `detail` = '') AND `target` IS NOT NULL AND `target` <> '';

-- 演示数据（可按需删 / 不需要可不执行）
INSERT INTO `hy_operation_log` (`id`,`addtime`,`operator`,`module`,`action`,`detail`)
SELECT 19001, NOW(), 'admin', '员工与系统', '系统初始化', '操作日志字段已对齐并可记录后台关键操作'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `hy_operation_log` WHERE `id` = 19001);
