-- 操作日志：与后台「操作人/模块/动作/详情」对齐
-- 若某列已存在，对应 ADD 会报错，可跳过该句继续执行

ALTER TABLE `hy_operation_log`
  ADD COLUMN `module` varchar(100) DEFAULT NULL COMMENT '模块' AFTER `operator`;

ALTER TABLE `hy_operation_log`
  ADD COLUMN `detail` varchar(500) DEFAULT NULL COMMENT '详情' AFTER `action`;

-- 旧字段 target → detail（无 target 列可跳过）
UPDATE `hy_operation_log`
SET `detail` = `target`
WHERE (`detail` IS NULL OR `detail` = '') AND `target` IS NOT NULL AND `target` <> '';

-- 演示数据（可按需删）
INSERT INTO `hy_operation_log` (`id`,`addtime`,`operator`,`module`,`action`,`detail`)
SELECT 19001, NOW(), 'admin', '员工与系统', '系统初始化', '操作日志字段已对齐并可记录后台关键操作'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `hy_operation_log` WHERE `id` = 19001);
