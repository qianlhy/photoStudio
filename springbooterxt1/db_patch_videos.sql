-- 选片素材绑定真实视频（hy_sample 目录，与 7001-7008 一一对应）
USE ssm48yhg;

UPDATE `hy_material` SET `video`='upload/hy_sample/hy_7001.mp4' WHERE `id`=7001;
UPDATE `hy_material` SET `video`='upload/hy_sample/hy_7002.mp4' WHERE `id`=7002;
UPDATE `hy_material` SET `video`='upload/hy_sample/hy_7003.mp4' WHERE `id`=7003;
UPDATE `hy_material` SET `video`='upload/hy_sample/hy_7004.mp4' WHERE `id`=7004;
UPDATE `hy_material` SET `video`='upload/hy_sample/hy_7005.mp4' WHERE `id`=7005;
UPDATE `hy_material` SET `video`='upload/hy_sample/hy_7006.mp4' WHERE `id`=7006;
UPDATE `hy_material` SET `video`='upload/hy_sample/hy_7007.mp4' WHERE `id`=7007;
UPDATE `hy_material` SET `video`='upload/hy_sample/hy_7008.mp4' WHERE `id`=7008;
