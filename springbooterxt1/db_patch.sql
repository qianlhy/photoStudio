-- 增量补丁：风格分类挂父级品类 + 消息模板活动
USE ssm48yhg;

-- pinlei 列已存在时跳过（重复执行安全）
SET @col_exists = (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = 'ssm48yhg' AND TABLE_NAME = 'leixing' AND COLUMN_NAME = 'pinlei'
);
SET @sql = IF(@col_exists = 0,
  'ALTER TABLE `leixing` ADD COLUMN `pinlei` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT ''所属品类(写真/宣传片)'' AFTER `leixing`',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE `leixing` SET `pinlei`='写真' WHERE `leixing` IN ('古风','日系','纪实','孕妇','儿童','韩式','现代类','类型1','类型2','类型3','类型4','类型5','类型6');
UPDATE `leixing` SET `pinlei`='宣传片' WHERE `leixing` IN ('个人MV','企业宣传片','产品短片');

INSERT INTO `config` (`name`, `value`) SELECT 'msgTplActivity', '【活动通知】{title}：{content}'
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `name`='msgTplActivity');
