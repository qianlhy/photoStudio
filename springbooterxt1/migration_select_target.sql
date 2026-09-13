-- 客户选片目标条数（后台可配置，Pad 选片读取）
ALTER TABLE `hy_customer`
  ADD COLUMN `select_target` int DEFAULT 15 COMMENT '选片目标条数(后台配置,Pad读取)' AFTER `publish_deadline`;

UPDATE `hy_customer` SET `select_target` = 15 WHERE `select_target` IS NULL;
