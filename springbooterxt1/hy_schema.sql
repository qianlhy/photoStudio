-- =====================================================================
-- 合意传媒三端系统 数据库 schema（D1-D8）
-- 表前缀 hy_，与旧照相馆表隔离。手动在 MySQL 库 ssm48yhg 执行本文件。
-- 文件/图片字段统一存 upload/xxx，前端用 baseUrl + 值访问。
-- =====================================================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ---------- D1 身份/组织与权限 ----------
DROP TABLE IF EXISTS `hy_employee`;
CREATE TABLE `hy_employee` (
  `id` bigint NOT NULL COMMENT '主键',
  `addtime` datetime DEFAULT NULL COMMENT '创建时间',
  `username` varchar(100) DEFAULT NULL COMMENT '登录账号/手机号',
  `password` varchar(200) DEFAULT NULL COMMENT '密码',
  `name` varchar(100) DEFAULT NULL COMMENT '姓名',
  `avatar` varchar(300) DEFAULT NULL COMMENT '头像',
  `phone` varchar(50) DEFAULT NULL COMMENT '手机号',
  `role` varchar(50) DEFAULT NULL COMMENT '角色:管理员/销售经理/拍摄/剪辑',
  `department` varchar(100) DEFAULT NULL COMMENT '部门',
  `status` varchar(20) DEFAULT '正常' COMMENT '状态:正常/停用',
  `permissions` varchar(500) DEFAULT NULL COMMENT '权限范围描述',
  `customer_count` int DEFAULT 0 COMMENT '名下客户数',
  `task_count` int DEFAULT 0 COMMENT '当前任务数',
  `lastlogin` datetime DEFAULT NULL COMMENT '最近登录',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工账号';

DROP TABLE IF EXISTS `hy_role`;
CREATE TABLE `hy_role` (
  `id` bigint NOT NULL,
  `addtime` datetime DEFAULT NULL,
  `rolename` varchar(100) DEFAULT NULL COMMENT '角色名',
  `permissions` text COMMENT '权限项(逗号分隔)',
  `remark` varchar(300) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限';

DROP TABLE IF EXISTS `hy_assignment`;
CREATE TABLE `hy_assignment` (
  `id` bigint NOT NULL,
  `addtime` datetime DEFAULT NULL,
  `customer_id` bigint DEFAULT NULL,
  `customer_name` varchar(200) DEFAULT NULL,
  `from_manager_id` bigint DEFAULT NULL,
  `from_manager_name` varchar(100) DEFAULT NULL,
  `to_manager_id` bigint DEFAULT NULL,
  `to_manager_name` varchar(100) DEFAULT NULL,
  `remark` varchar(300) DEFAULT NULL,
  `operator` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户交接记录';

-- ---------- D2 客户与关系经营 ----------
DROP TABLE IF EXISTS `hy_customer`;
CREATE TABLE `hy_customer` (
  `id` bigint NOT NULL,
  `addtime` datetime DEFAULT NULL,
  `name` varchar(200) DEFAULT NULL COMMENT '客户/门店名称',
  `contact` varchar(100) DEFAULT NULL COMMENT '联系人',
  `phone` varchar(50) DEFAULT NULL,
  `industry` varchar(50) DEFAULT NULL COMMENT '行业大类:餐饮/建筑/企业',
  `biztype` varchar(50) DEFAULT NULL COMMENT '业态/小类:火锅/奶茶...',
  `scale` varchar(50) DEFAULT NULL COMMENT '体量',
  `manager_id` bigint DEFAULT NULL COMMENT '业务经理id',
  `manager_name` varchar(100) DEFAULT NULL,
  `follow_status` varchar(30) DEFAULT '跟进中' COMMENT '跟进中/已成交/待付款',
  `intention` varchar(10) DEFAULT '中' COMMENT '高/中/低',
  `tags` varchar(300) DEFAULT NULL COMMENT '客户标签(逗号)',
  `preference` varchar(300) DEFAULT NULL COMMENT '内容偏好(逗号)',
  `avatar` varchar(300) DEFAULT NULL COMMENT '头像/封面',
  `last_follow_time` datetime DEFAULT NULL,
  `deal_count` int DEFAULT 0 COMMENT '成交次数',
  `unpaid_count` int DEFAULT 0 COMMENT '未成订单',
  `unpaid_amount` decimal(12,2) DEFAULT 0 COMMENT '未付款金额',
  `satisfaction` int DEFAULT 5 COMMENT '满意度1-5',
  `selected_count` int DEFAULT 0 COMMENT '已选',
  `shot_count` int DEFAULT 0 COMMENT '已拍',
  `delivered_count` int DEFAULT 0 COMMENT '已交付',
  `published_count` int DEFAULT 0 COMMENT '已发布',
  `remain_count` int DEFAULT 0 COMMENT '剩余内容/库存',
  `publish_days` int DEFAULT 0 COMMENT '预计可发布天数',
  `publish_deadline` varchar(50) DEFAULT NULL COMMENT '内容预计发布至',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户档案';

DROP TABLE IF EXISTS `hy_follow_record`;
CREATE TABLE `hy_follow_record` (
  `id` bigint NOT NULL,
  `addtime` datetime DEFAULT NULL,
  `customer_id` bigint DEFAULT NULL,
  `customer_name` varchar(200) DEFAULT NULL,
  `recorder_id` bigint DEFAULT NULL,
  `recorder_name` varchar(100) DEFAULT NULL,
  `contact_result` varchar(50) DEFAULT NULL COMMENT '已联系/未接通/客户考虑中/已发送方案/已确认续拍',
  `intention` varchar(10) DEFAULT NULL COMMENT '高/中/低',
  `next_action` varchar(50) DEFAULT NULL COMMENT '下一步动作',
  `next_time` datetime DEFAULT NULL COMMENT '下次跟进时间',
  `remark` text COMMENT '自由备注',
  `summary` text COMMENT '自动摘要',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='跟进记录';

DROP TABLE IF EXISTS `hy_follow_task`;
CREATE TABLE `hy_follow_task` (
  `id` bigint NOT NULL,
  `addtime` datetime DEFAULT NULL,
  `customer_id` bigint DEFAULT NULL,
  `customer_name` varchar(200) DEFAULT NULL,
  `owner_id` bigint DEFAULT NULL,
  `owner_name` varchar(100) DEFAULT NULL,
  `task_time` datetime DEFAULT NULL,
  `action` varchar(100) DEFAULT NULL,
  `status` varchar(20) DEFAULT '待办' COMMENT '待办/已完成',
  `source_record_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='下一步任务';

-- ---------- D3 对标素材与短片 ----------
DROP TABLE IF EXISTS `hy_industry`;
CREATE TABLE `hy_industry` (
  `id` bigint NOT NULL,
  `addtime` datetime DEFAULT NULL,
  `name` varchar(100) DEFAULT NULL COMMENT '分类名',
  `parent_id` bigint DEFAULT 0 COMMENT '父id,0为大类',
  `parent_name` varchar(100) DEFAULT NULL,
  `level` int DEFAULT 1 COMMENT '1大类/2小类',
  `cover` varchar(300) DEFAULT NULL COMMENT '封面',
  `material_count` int DEFAULT 0 COMMENT '作品数量',
  `heat` int DEFAULT 0 COMMENT '热度',
  `sort` int DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='行业分类树';

DROP TABLE IF EXISTS `hy_material`;
CREATE TABLE `hy_material` (
  `id` bigint NOT NULL,
  `addtime` datetime DEFAULT NULL,
  `title` varchar(200) DEFAULT NULL COMMENT '标题',
  `cover` varchar(300) DEFAULT NULL COMMENT '封面',
  `video` varchar(300) DEFAULT NULL COMMENT '视频',
  `duration` int DEFAULT 0 COMMENT '时长(秒)',
  `industry_big` varchar(50) DEFAULT NULL COMMENT '行业大类',
  `industry_sub` varchar(50) DEFAULT NULL COMMENT '业态/小类',
  `content_type` varchar(30) DEFAULT NULL COMMENT '厨过程/教知识/讲故事/说观点/硬广',
  `tags` varchar(300) DEFAULT NULL,
  `like_count` int DEFAULT 0,
  `dislike_count` int DEFAULT 0,
  `favorite_count` int DEFAULT 0,
  `view_count` int DEFAULT 0,
  `used_count` int DEFAULT 0 COMMENT '选用次数',
  `heat` int DEFAULT 0 COMMENT '热度',
  `status` varchar(20) DEFAULT '上架' COMMENT '上架/下架/回收站',
  `recommend` int DEFAULT 0 COMMENT '推荐素材',
  `source` varchar(200) DEFAULT NULL COMMENT '来源链接',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对标素材短视频';

-- ---------- D5 选片/方案/交付/质量 ----------
DROP TABLE IF EXISTS `hy_selection_session`;
CREATE TABLE `hy_selection_session` (
  `id` bigint NOT NULL,
  `addtime` datetime DEFAULT NULL,
  `customer_id` bigint DEFAULT NULL,
  `customer_name` varchar(200) DEFAULT NULL,
  `manager_id` bigint DEFAULT NULL,
  `manager_name` varchar(100) DEFAULT NULL,
  `industry` varchar(50) DEFAULT NULL,
  `biztype` varchar(50) DEFAULT NULL,
  `target_count` int DEFAULT 0 COMMENT '目标数',
  `selected_count` int DEFAULT 0 COMMENT '已选数',
  `liked` text COMMENT '喜欢素材id(逗号)',
  `disliked` text COMMENT '不喜欢素材id(逗号)',
  `c_process` int DEFAULT 0 COMMENT '厨过程',
  `c_knowledge` int DEFAULT 0 COMMENT '教知识',
  `c_story` int DEFAULT 0 COMMENT '讲故事',
  `c_opinion` int DEFAULT 0 COMMENT '说观点',
  `c_ad` int DEFAULT 0 COMMENT '硬广',
  `status` varchar(20) DEFAULT '进行中' COMMENT '进行中/已结束',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='选片会话';

DROP TABLE IF EXISTS `hy_content_plan`;
CREATE TABLE `hy_content_plan` (
  `id` bigint NOT NULL,
  `addtime` datetime DEFAULT NULL,
  `customer_id` bigint DEFAULT NULL,
  `customer_name` varchar(200) DEFAULT NULL,
  `session_id` bigint DEFAULT NULL,
  `original_selection` text COMMENT '客户原始选择(json)',
  `r_process` int DEFAULT 0,
  `r_knowledge` int DEFAULT 0,
  `r_story` int DEFAULT 0,
  `r_opinion` int DEFAULT 0,
  `r_ad` int DEFAULT 0,
  `total_count` int DEFAULT 0,
  `final_materials` text COMMENT '最终参考素材id(逗号)',
  `confirmed` int DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容方案(五类配方)';

DROP TABLE IF EXISTS `hy_deliverable`;
CREATE TABLE `hy_deliverable` (
  `id` bigint NOT NULL,
  `addtime` datetime DEFAULT NULL,
  `title` varchar(200) DEFAULT NULL COMMENT '作品名称',
  `cover` varchar(300) DEFAULT NULL,
  `video` varchar(300) DEFAULT NULL,
  `duration` int DEFAULT 0,
  `order_id` bigint DEFAULT NULL,
  `order_no` varchar(100) DEFAULT NULL,
  `customer_id` bigint DEFAULT NULL,
  `customer_name` varchar(200) DEFAULT NULL,
  `industry` varchar(50) DEFAULT NULL,
  `content_type` varchar(30) DEFAULT NULL,
  `shooter_name` varchar(100) DEFAULT NULL,
  `editor_name` varchar(100) DEFAULT NULL,
  `view_status` varchar(20) DEFAULT '未查看' COMMENT '已查看/未查看',
  `download_status` varchar(20) DEFAULT '未下载' COMMENT '已下载/未下载',
  `download_time` datetime DEFAULT NULL,
  `satisfaction` int DEFAULT 0 COMMENT '满意度1-5',
  `customer_comment` varchar(500) DEFAULT NULL COMMENT '客户评价',
  `quality_flag` int DEFAULT 0 COMMENT '优质作品标记',
  `reuse_value` varchar(300) DEFAULT NULL COMMENT '可复用价值',
  `apply_industry` varchar(200) DEFAULT NULL COMMENT '适用行业',
  `status` varchar(20) DEFAULT '上架' COMMENT '上架/下架',
  `sort` int DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成品与优质作品';

-- ---------- D4 订单与执行 ----------
DROP TABLE IF EXISTS `hy_package`;
CREATE TABLE `hy_package` (
  `id` bigint NOT NULL,
  `addtime` datetime DEFAULT NULL,
  `name` varchar(200) DEFAULT NULL COMMENT '套餐名',
  `video_count` int DEFAULT 0 COMMENT '视频条数',
  `price` decimal(12,2) DEFAULT 0,
  `industry` varchar(50) DEFAULT NULL,
  `remark` varchar(300) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套餐';

DROP TABLE IF EXISTS `hy_order`;
CREATE TABLE `hy_order` (
  `id` bigint NOT NULL,
  `addtime` datetime DEFAULT NULL,
  `order_no` varchar(100) DEFAULT NULL COMMENT '订单号',
  `customer_id` bigint DEFAULT NULL,
  `customer_name` varchar(200) DEFAULT NULL,
  `package_name` varchar(200) DEFAULT NULL,
  `video_count` int DEFAULT 0 COMMENT '本次数量',
  `completed_count` int DEFAULT 0 COMMENT '已完成数',
  `manager_id` bigint DEFAULT NULL,
  `manager_name` varchar(100) DEFAULT NULL COMMENT '负责人/业务经理',
  `shooter_id` bigint DEFAULT NULL,
  `shooter_name` varchar(100) DEFAULT NULL COMMENT '拍摄人员',
  `editor_id` bigint DEFAULT NULL,
  `editor_name` varchar(100) DEFAULT NULL COMMENT '剪辑人员',
  `industry` varchar(50) DEFAULT NULL,
  `biztype` varchar(50) DEFAULT NULL,
  `shoot_date` datetime DEFAULT NULL COMMENT '拍摄日期',
  `target_date` datetime DEFAULT NULL COMMENT '内部目标日',
  `deliver_date` datetime DEFAULT NULL COMMENT '承诺交付日',
  `status` varchar(20) DEFAULT '待拍摄' COMMENT '待拍摄/待交付/已完成',
  `recipe` varchar(200) DEFAULT NULL COMMENT '五类配方摘要',
  `abnormal` varchar(50) DEFAULT NULL COMMENT '异常状态',
  `quality_flag` int DEFAULT 0 COMMENT '优质作品数',
  `amount` decimal(12,2) DEFAULT 0 COMMENT '金额(后台用)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单';

DROP TABLE IF EXISTS `hy_content_item`;
CREATE TABLE `hy_content_item` (
  `id` bigint NOT NULL,
  `addtime` datetime DEFAULT NULL,
  `order_id` bigint DEFAULT NULL,
  `title` varchar(200) DEFAULT NULL COMMENT '内容标题',
  `content_type` varchar(30) DEFAULT NULL,
  `material_ref` bigint DEFAULT NULL COMMENT '参考素材id',
  `cover` varchar(300) DEFAULT NULL,
  `status` int DEFAULT 0 COMMENT '0未完成/1完成',
  `sort` int DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单内容清单';

-- ---------- D6 发布/余量/提醒/续拍 ----------
DROP TABLE IF EXISTS `hy_renewal`;
CREATE TABLE `hy_renewal` (
  `id` bigint NOT NULL,
  `addtime` datetime DEFAULT NULL,
  `customer_id` bigint DEFAULT NULL,
  `customer_name` varchar(200) DEFAULT NULL,
  `remain_count` int DEFAULT 0,
  `publish_days` int DEFAULT 0,
  `status` varchar(20) DEFAULT '待跟进',
  `owner_name` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='续拍跟进';

-- ---------- D7 消息/行动事项/任务 ----------
DROP TABLE IF EXISTS `hy_action_item`;
CREATE TABLE `hy_action_item` (
  `id` bigint NOT NULL,
  `addtime` datetime DEFAULT NULL,
  `type` varchar(30) DEFAULT NULL COMMENT '待付款/客诉/制作预警/库存不足',
  `customer_id` bigint DEFAULT NULL,
  `customer_name` varchar(200) DEFAULT NULL,
  `order_id` bigint DEFAULT NULL,
  `title` varchar(300) DEFAULT NULL COMMENT '事项描述',
  `owner_id` bigint DEFAULT NULL,
  `owner_name` varchar(100) DEFAULT NULL,
  `intention` varchar(10) DEFAULT NULL,
  `inner_progress` int DEFAULT 0 COMMENT '内部周期百分比',
  `can_intervene` int DEFAULT 0 COMMENT '是否可干预',
  `status` varchar(20) DEFAULT '待处理' COMMENT '待处理/处理中/已完成',
  `belong` varchar(20) DEFAULT '销售' COMMENT '销售/编导',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='行动事项';

DROP TABLE IF EXISTS `hy_message`;
CREATE TABLE `hy_message` (
  `id` bigint NOT NULL,
  `addtime` datetime DEFAULT NULL,
  `from_type` varchar(20) DEFAULT NULL COMMENT '客户/系统/销售',
  `customer_id` bigint DEFAULT NULL,
  `customer_name` varchar(200) DEFAULT NULL,
  `title` varchar(300) DEFAULT NULL,
  `content` text,
  `belong` varchar(20) DEFAULT '销售' COMMENT '销售/编导',
  `status` varchar(20) DEFAULT '未处理',
  `isread` int DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息';

-- ---------- D8 统计/审计/配置 ----------
DROP TABLE IF EXISTS `hy_operation_log`;
CREATE TABLE `hy_operation_log` (
  `id` bigint NOT NULL,
  `addtime` datetime DEFAULT NULL,
  `operator` varchar(100) DEFAULT NULL,
  `action` varchar(300) DEFAULT NULL,
  `target` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志';

DROP TABLE IF EXISTS `hy_system_config`;
CREATE TABLE `hy_system_config` (
  `id` bigint NOT NULL,
  `name` varchar(100) DEFAULT NULL,
  `value` varchar(1000) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置';

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================================
-- 种子数据（与 UI 示例对齐）
-- =====================================================================
-- 系统配置/品牌
INSERT INTO `hy_system_config` (`id`,`name`,`value`) VALUES
 (1,'brandName','合意传媒'),
 (2,'brandSlogan','短视频·内容·增长'),
 (3,'customerBrandName','影集');

-- 员工（密码均 123456 明文，首次登录自动升级为密文）
INSERT INTO `hy_employee` (`id`,`addtime`,`username`,`password`,`name`,`avatar`,`phone`,`role`,`department`,`status`,`permissions`,`customer_count`,`task_count`,`lastlogin`) VALUES
 (1001,'2026-06-01 09:00:00','admin','123456','管理员',NULL,'13800130000','管理员','管理部','正常','全部权限',0,0,'2026-06-20 10:32:00'),
 (1002,'2026-06-01 09:00:00','ajie','123456','阿杰',NULL,'13800138002','销售经理','销售部','正常','管理自己客户,跟进,查询订单',26,14,'2026-06-20 09:18:00'),
 (1003,'2026-06-01 09:00:00','wangxu','123456','王旭',NULL,'13800138003','销售经理','销售部','正常','管理自己客户,跟进,查询订单',36,18,'2026-06-20 08:45:00'),
 (1004,'2026-06-01 09:00:00','zhangyutong','123456','张雨桐',NULL,'13800138001','销售经理','销售部','正常','管理自己客户,跟进,查询订单',32,16,'2026-06-19 17:30:00'),
 (1005,'2026-06-01 09:00:00','liuze','123456','刘泽',NULL,'13800138005','剪辑','内容制作部','正常','查看分配任务,上传成品',7,5,'2026-06-19 20:53:00'),
 (1006,'2026-06-01 09:00:00','zhouqiang','123456','周强',NULL,'13800138007','拍摄','拍摄部','正常','仅记录,不可上传',4,3,'2026-06-19 19:21:00');

INSERT INTO `hy_role` (`id`,`addtime`,`rolename`,`permissions`,`remark`) VALUES
 (1,'2026-06-01 09:00:00','管理员','全部权限','系统超级管理员'),
 (2,'2026-06-01 09:00:00','销售经理','客户管理,跟进,选片,订单查看','负责接待与客户经营'),
 (3,'2026-06-01 09:00:00','拍摄','任务查看,记录','拍摄人员仅记录'),
 (4,'2026-06-01 09:00:00','剪辑','任务查看,上传成品','剪辑人员可上传');

-- 行业分类（大类 + 小类）
INSERT INTO `hy_industry` (`id`,`addtime`,`name`,`parent_id`,`parent_name`,`level`,`cover`,`material_count`,`heat`,`sort`) VALUES
 (10,'2026-06-01 09:00:00','餐饮',0,NULL,1,'upload/studio_cover_1.jpg',708,100,1),
 (20,'2026-06-01 09:00:00','建筑',0,NULL,1,'upload/studio_cover_4.jpg',358,70,2),
 (30,'2026-06-01 09:00:00','企业',0,NULL,1,'upload/studio_cover_8.jpg',220,60,3),
 (101,'2026-06-01 09:00:00','火锅',10,'餐饮',2,'upload/studio_cover_1.jpg',186,100,1),
 (102,'2026-06-01 09:00:00','奶茶',10,'餐饮',2,'upload/studio_cover_3.jpg',142,80,2),
 (103,'2026-06-01 09:00:00','小吃',10,'餐饮',2,'upload/studio_cover_2.jpg',128,70,3),
 (104,'2026-06-01 09:00:00','快餐',10,'餐饮',2,'upload/studio_cover_6.jpg',96,50,4),
 (105,'2026-06-01 09:00:00','甜点',10,'餐饮',2,'upload/studio_cover_5.jpg',72,40,5),
 (106,'2026-06-01 09:00:00','糖水',10,'餐饮',2,'upload/studio_cover_7.jpg',48,30,6),
 (107,'2026-06-01 09:00:00','零食',10,'餐饮',2,'upload/studio_cover_2.jpg',36,20,7),
 (201,'2026-06-01 09:00:00','别墅',20,'建筑',2,'upload/studio_cover_4.jpg',118,60,1),
 (202,'2026-06-01 09:00:00','装修',20,'建筑',2,'upload/studio_cover_4.jpg',104,50,2),
 (203,'2026-06-01 09:00:00','建材',20,'建筑',2,'upload/studio_cover_4.jpg',92,40,3),
 (204,'2026-06-01 09:00:00','辅材',20,'建筑',2,'upload/studio_cover_4.jpg',44,20,4),
 (301,'2026-06-01 09:00:00','生产',30,'企业',2,'upload/studio_cover_8.jpg',82,40,1),
 (302,'2026-06-01 09:00:00','销售',30,'企业',2,'upload/studio_cover_8.jpg',76,40,2),
 (303,'2026-06-01 09:00:00','艺术教育',30,'企业',2,'upload/studio_cover_8.jpg',68,30,3),
 (304,'2026-06-01 09:00:00','文化教育',30,'企业',2,'upload/studio_cover_8.jpg',54,30,4);

-- 套餐
INSERT INTO `hy_package` (`id`,`addtime`,`name`,`video_count`,`price`,`industry`,`remark`) VALUES
 (5001,'2026-06-01 09:00:00','门店增长套餐',10,9800.00,'餐饮','含拍摄+剪辑10条短视频'),
 (5002,'2026-06-01 09:00:00','品牌形象套餐',8,8800.00,'建筑','品牌形象短视频8条'),
 (5003,'2026-06-01 09:00:00','招生获客套餐',12,12800.00,'企业','教育获客短视频12条'),
 (5004,'2026-06-01 09:00:00','企业宣传套餐',7,7600.00,'企业','企业宣传短视频7条');

-- 客户
INSERT INTO `hy_customer` (`id`,`addtime`,`name`,`contact`,`phone`,`industry`,`biztype`,`scale`,`manager_id`,`manager_name`,`follow_status`,`intention`,`tags`,`preference`,`avatar`,`last_follow_time`,`deal_count`,`unpaid_count`,`unpaid_amount`,`satisfaction`,`selected_count`,`shot_count`,`delivered_count`,`published_count`,`remain_count`,`publish_days`,`publish_deadline`) VALUES
 (6001,'2026-05-01 09:00:00','林女士·山城火锅','林女士','13823888881','餐饮','火锅','连锁',1002,'阿杰','跟进中','高','重点客户,长期合作','硬广,随过程,真实烟火气','upload/studio_cover_1.jpg','2026-06-20 10:30:00',3,2,58600.00,5,15,10,8,4,4,8,'6月28日'),
 (6002,'2026-05-02 09:00:00','王先生·名筑瓷砖','王经理','13823888882','建筑','建材','区域',1004,'张雨桐','已成交','中','成长客户,活跃品牌','品牌形象','upload/studio_cover_4.jpg','2026-06-12 16:45:00',5,0,0,5,12,8,6,9,9,10,'7月10日'),
 (6003,'2026-05-03 09:00:00','启星口才·教培','刘老师','13823888883','企业','文化教育','单店',1003,'王旭','跟进中','高','潜力客户','讲故事,说观点','upload/studio_cover_8.jpg','2026-06-18 11:05:00',1,1,21400.00,4,18,12,8,5,18,30,'8月12日'),
 (6004,'2026-05-04 09:00:00','陈老板·奶茶店','陈老板','13823888884','餐饮','奶茶','单店',1002,'阿杰','待付款','中','新客户','随过程','upload/studio_cover_3.jpg','2026-06-15 09:00:00',0,1,32800.00,4,7,3,0,0,0,0,NULL),
 (6005,'2026-05-05 09:00:00','杭州葛九香火锅','周总','13823888885','餐饮','火锅','连锁',1004,'张雨桐','跟进中','高','重点客户','硬广,真实烟火气','upload/studio_cover_1.jpg','2026-05-31 10:30:00',3,2,58600.00,5,10,6,3,2,5,8,NULL);

-- 素材短视频（火锅类，复用 upload 现有图片做封面；video 暂留空，可后台上传）
INSERT INTO `hy_material` (`id`,`addtime`,`title`,`cover`,`video`,`duration`,`industry_big`,`industry_sub`,`content_type`,`tags`,`like_count`,`dislike_count`,`favorite_count`,`view_count`,`used_count`,`heat`,`status`,`recommend`,`source`) VALUES
 (7001,'2026-05-20 10:30:00','3秒上菜挑战','upload/studio_cover_1.jpg','upload/hy_sample/hy_7001.mp4',32,'餐饮','火锅','硬广','硬广,强节奏',2438,210,168,2438,1025,98,'上架',1,NULL),
 (7002,'2026-05-21 09:18:00','锅底煎制','upload/studio_cover_2.jpg','upload/hy_sample/hy_7002.mp4',19,'餐饮','火锅','厨过程','厨过程',1865,231,132,1865,789,80,'上架',0,NULL),
 (7003,'2026-05-22 16:45:00','牛肉知识','upload/studio_cover_5.jpg','upload/hy_sample/hy_7003.mp4',28,'餐饮','火锅','教知识','教知识,料方',1245,312,213,1245,560,70,'上架',0,NULL),
 (7004,'2026-05-23 11:10:00','老板观点','upload/studio_cover_6.jpg','upload/hy_sample/hy_7004.mp4',30,'餐饮','火锅','说观点','说观点',1204,197,97,1204,455,60,'上架',1,NULL),
 (7005,'2026-05-24 09:05:00','门店日常','upload/studio_cover_7.jpg','upload/hy_sample/hy_7005.mp4',24,'餐饮','火锅','讲故事','讲故事',876,248,76,876,410,55,'上架',0,NULL),
 (7006,'2026-05-25 14:00:00','食材准备','upload/studio_cover_3.jpg','upload/hy_sample/hy_7006.mp4',26,'餐饮','火锅','厨过程','厨过程',1183,215,131,1183,612,65,'上架',0,NULL),
 (7007,'2026-05-26 10:00:00','秘制锅底炒料','upload/studio_cover_4.jpg','upload/hy_sample/hy_7007.mp4',31,'餐饮','火锅','厨过程','厨过程',940,120,88,940,300,50,'上架',0,NULL),
 (7008,'2026-05-27 10:00:00','现切鲜牛肉','upload/studio_cover_1.jpg','upload/hy_sample/hy_7008.mp4',26,'餐饮','火锅','硬广','硬广',1320,140,120,1320,520,62,'上架',0,NULL);

-- 选片会话（林女士本次接待，已选8条）
INSERT INTO `hy_selection_session` (`id`,`addtime`,`customer_id`,`customer_name`,`manager_id`,`manager_name`,`industry`,`biztype`,`target_count`,`selected_count`,`liked`,`disliked`,`c_process`,`c_knowledge`,`c_story`,`c_opinion`,`c_ad`,`status`) VALUES
 (8001,'2026-06-20 10:00:00',6001,'林女士·山城火锅',1002,'阿杰','餐饮','火锅',15,8,'7001,7002,7006','7005',2,1,0,1,4,'进行中');

-- 订单（与 UI 1-4/3-3 对齐）
INSERT INTO `hy_order` (`id`,`addtime`,`order_no`,`customer_id`,`customer_name`,`package_name`,`video_count`,`completed_count`,`manager_id`,`manager_name`,`shooter_id`,`shooter_name`,`editor_id`,`editor_name`,`industry`,`biztype`,`shoot_date`,`target_date`,`deliver_date`,`status`,`recipe`,`abnormal`,`quality_flag`,`amount`) VALUES
 (9001,'2026-06-16 09:00:00','YJ-0620-018',6001,'林女士·山城火锅','门店增长套餐',10,6,1002,'阿杰',1006,'周强',1005,'刘泽','餐饮','火锅','2026-06-16 09:00:00','2026-06-25 18:00:00','2026-06-28 18:00:00','待交付','硬广4·厨过程2·教知识2·说观点1·讲故事1',NULL,2,9800.00),
 (9002,'2026-06-10 09:00:00','YJ-0610-008',6002,'王先生·名筑瓷砖','品牌形象套餐',8,3,1004,'张雨桐',1006,'周强',1005,'刘泽','建筑','建材','2026-06-12 09:00:00','2026-06-20 18:00:00','2026-06-23 18:00:00','待交付','品牌形象8条',NULL,1,8800.00),
 (9003,'2026-06-08 09:00:00','YJ-0608-003',6003,'启星口才·教培','招生获客套餐',12,10,1003,'王旭',1006,'周强',1005,'刘泽','企业','文化教育','2026-06-09 09:00:00','2026-06-18 18:00:00','2026-06-21 18:00:00','待交付','讲故事6·说观点6',NULL,0,12800.00),
 (9004,'2026-05-20 09:00:00','DD20250520045',6005,'杭州葛九香火锅','企业宣传套餐',11,7,1004,'张雨桐',1006,'周强',1005,'刘泽','餐饮','火锅','2026-05-22 09:00:00','2026-06-05 18:00:00','2026-06-07 18:00:00','待交付','企业宣传11条','交付延迟',2,9800.00),
 (9005,'2026-04-20 09:00:00','DD20250420011',6002,'王先生·名筑瓷砖','品牌形象套餐',8,8,1004,'张雨桐',1006,'周强',1005,'刘泽','建筑','建材','2026-04-22 09:00:00','2026-05-01 18:00:00','2026-05-03 18:00:00','已完成','品牌形象8条',NULL,1,8800.00);

-- 订单内容清单（订单9001，10条）
INSERT INTO `hy_content_item` (`id`,`addtime`,`order_id`,`title`,`content_type`,`material_ref`,`cover`,`status`,`sort`) VALUES
 (91001,'2026-06-16 09:00:00',9001,'3秒上菜','硬广',7001,'upload/studio_cover_1.jpg',1,1),
 (91002,'2026-06-16 09:00:00',9001,'锅底故事','讲故事',7005,'upload/studio_cover_2.jpg',1,2),
 (91003,'2026-06-16 09:00:00',9001,'食材科普','教知识',7003,'upload/studio_cover_5.jpg',1,3),
 (91004,'2026-06-16 09:00:00',9001,'锅底故事','讲故事',7005,'upload/studio_cover_6.jpg',1,4),
 (91005,'2026-06-16 09:00:00',9001,'用餐过程','厨过程',7002,'upload/studio_cover_7.jpg',1,5),
 (91006,'2026-06-16 09:00:00',9001,'门店环境','讲故事',7005,'upload/studio_cover_3.jpg',1,6),
 (91007,'2026-06-16 09:00:00',9001,'食材科普','教知识',7003,'upload/studio_cover_4.jpg',0,7),
 (91008,'2026-06-16 09:00:00',9001,'锅底故事','讲故事',7005,'upload/studio_cover_1.jpg',0,8),
 (91009,'2026-06-16 09:00:00',9001,'用餐过程','厨过程',7002,'upload/studio_cover_2.jpg',0,9),
 (91010,'2026-06-16 09:00:00',9001,'老板观点','说观点',7004,'upload/studio_cover_6.jpg',0,10);

-- 成品（客户小程序内容页 / 后台成品页）
INSERT INTO `hy_deliverable` (`id`,`addtime`,`title`,`cover`,`video`,`duration`,`order_id`,`order_no`,`customer_id`,`customer_name`,`industry`,`content_type`,`shooter_name`,`editor_name`,`view_status`,`download_status`,`satisfaction`,`customer_comment`,`quality_flag`,`reuse_value`,`apply_industry`,`status`,`sort`) VALUES
 (10001,'2026-06-20 12:00:00','上菜挑战','upload/studio_cover_1.jpg',NULL,32,9001,'YJ-0620-018',6001,'林女士·山城火锅','餐饮','硬广','周强','刘泽','已查看','已下载',5,'画面节奏把控好',1,'科技转场可复用','科技信息,互联网,软件服务','上架',1),
 (10002,'2026-06-20 12:00:00','锅底煎制','upload/studio_cover_2.jpg',NULL,19,9001,'YJ-0620-018',6001,'林女士·山城火锅','餐饮','厨过程','周强','刘泽','已查看','未下载',5,NULL,0,NULL,NULL,'上架',2),
 (10003,'2026-06-20 12:00:00','牛肉知识','upload/studio_cover_5.jpg',NULL,28,9001,'YJ-0620-018',6001,'林女士·山城火锅','餐饮','教知识','周强','刘泽','未查看','未下载',0,NULL,0,NULL,NULL,'上架',3),
 (10004,'2026-06-20 12:00:00','老板观点','upload/studio_cover_6.jpg',NULL,30,9001,'YJ-0620-018',6001,'林女士·山城火锅','餐饮','说观点','周强','刘泽','已查看','已下载',5,NULL,0,NULL,NULL,'上架',4),
 (10005,'2026-06-20 12:00:00','门店日常','upload/studio_cover_7.jpg',NULL,24,9001,'YJ-0620-018',6001,'林女士·山城火锅','餐饮','硬广','周强','刘泽','未查看','未下载',0,NULL,0,NULL,NULL,'上架',5),
 (10006,'2026-06-20 12:00:00','食材准备','upload/studio_cover_3.jpg',NULL,26,9001,'YJ-0620-018',6001,'林女士·山城火锅','餐饮','厨过程','周强','刘泽','未查看','未下载',0,NULL,0,NULL,NULL,'上架',6);

-- 跟进记录
INSERT INTO `hy_follow_record` (`id`,`addtime`,`customer_id`,`customer_name`,`recorder_id`,`recorder_name`,`contact_result`,`intention`,`next_action`,`next_time`,`remark`,`summary`) VALUES
 (11001,'2026-06-20 16:42:00',6001,'林女士·山城火锅',1002,'阿杰','已联系','高','发送续拍方案','2026-06-22 10:00:00','客户希望增加门店故事类内容，预算约3000元，周末方便拍摄。','6月20日，阿杰已联系林女士。客户意向较高，已发送方案，计划6月22日10:00再次跟进。');

-- 跟进任务
INSERT INTO `hy_follow_task` (`id`,`addtime`,`customer_id`,`customer_name`,`owner_id`,`owner_name`,`task_time`,`action`,`status`,`source_record_id`) VALUES
 (12001,'2026-06-20 16:42:00',6001,'林女士·山城火锅',1002,'阿杰','2026-06-22 10:00:00','发送续拍方案','待办',11001),
 (12002,'2026-06-20 13:30:00',6003,'启星口才·教培',1003,'王旭','2026-06-20 13:30:00','确认拍摄日期','待办',NULL),
 (12003,'2026-06-20 15:45:00',6004,'陈老板·奶茶店',1002,'阿杰','2026-06-20 15:45:00','提醒完成付款','待办',NULL);

-- 行动事项（销售行动中心）
INSERT INTO `hy_action_item` (`id`,`addtime`,`type`,`customer_id`,`customer_name`,`order_id`,`title`,`owner_id`,`owner_name`,`intention`,`inner_progress`,`can_intervene`,`status`,`belong`) VALUES
 (13001,'2026-06-20 09:00:00','待付款',6001,'林女士·山城火锅',NULL,'内容方案已确认，等待客户完成付款',1002,'阿杰','高',0,0,'待处理','销售'),
 (13002,'2026-06-20 09:00:00','制作预警',6002,'王先生·名筑瓷砖',9002,'内部制作周期已过 2/3，当前仅完成 4/8',1004,'张雨桐',NULL,67,1,'待处理','销售'),
 (13003,'2026-06-20 09:00:00','客诉',6003,'启星口才·教培',9003,'客户反馈第3条视频节奏与预期不符',1003,'王旭',NULL,0,0,'处理中','销售'),
 (13004,'2026-06-20 09:00:00','库存不足',6004,'陈老板·奶茶店',NULL,'剩余4条内容，预计还可发布8天',1002,'阿杰',NULL,0,0,'待处理','销售');

-- 客户消息
INSERT INTO `hy_message` (`id`,`addtime`,`from_type`,`customer_id`,`customer_name`,`title`,`content`,`belong`,`status`,`isread`) VALUES
 (14001,'2026-06-20 09:30:00','客户',6001,'林女士·山城火锅','付款咨询','付款大概什么时候可以？','销售','待处理',0),
 (14002,'2026-06-20 09:35:00','客户',6002,'王先生·名筑瓷砖','拍摄时间','想调整拍摄时间','编导','已自动分配编导',0);
