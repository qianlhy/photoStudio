-- ============================================================
-- 素材数量核对（Pad「行业素材库」与后端接口一致）
-- 前端/接口统计规则：仅统计 hy_material.status = '上架'
-- ============================================================

-- 1) 总览：全部素材 vs 已上架（前端「全部 X 条」用后者）
SELECT
  COUNT(*) AS total_all,
  SUM(CASE WHEN status = '上架' THEN 1 ELSE 0 END) AS total_on_shelf,
  SUM(CASE WHEN status = '下架' THEN 1 ELSE 0 END) AS total_off_shelf,
  SUM(CASE WHEN status = '回收站' THEN 1 ELSE 0 END) AS total_recycle,
  SUM(CASE WHEN status IS NULL OR status NOT IN ('上架','下架','回收站') THEN 1 ELSE 0 END) AS total_other_status
FROM hy_material;

-- 2) 若 total_on_shelf = 0，则前端显示 0 是正确的
-- 快速判断（只看一行结果）
SELECT
  CASE
    WHEN SUM(CASE WHEN status = '上架' THEN 1 ELSE 0 END) = 0 THEN '是：已上架素材为 0，前端应显示 0'
    ELSE CONCAT('否：已上架素材有 ', SUM(CASE WHEN status = '上架' THEN 1 ELSE 0 END), ' 条')
  END AS frontend_should_show_zero
FROM hy_material;

-- 3) 按行业大类统计（已上架）
SELECT
  IFNULL(industry_big, '(未填大类)') AS industry_big,
  COUNT(*) AS cnt
FROM hy_material
WHERE status = '上架'
GROUP BY industry_big
ORDER BY cnt DESC;

-- 4) 按业态/小类统计（已上架）— 对应素材库卡片上的数字
SELECT
  IFNULL(industry_sub, '(未填小类)') AS industry_sub,
  IFNULL(industry_big, '(未填大类)') AS industry_big,
  COUNT(*) AS cnt
FROM hy_material
WHERE status = '上架'
GROUP BY industry_sub, industry_big
ORDER BY cnt DESC;

-- 5) 对比：行业表里的假数字 material_count vs 真实已上架数量
SELECT
  i.id,
  i.name,
  i.level,
  i.parent_name,
  i.material_count AS seed_count_in_db,
  CASE
    WHEN i.level = 1 THEN (
      SELECT COUNT(*) FROM hy_material m
      WHERE m.status = '上架' AND m.industry_big = i.name
    )
    WHEN i.level = 2 THEN (
      SELECT COUNT(*) FROM hy_material m
      WHERE m.status = '上架' AND m.industry_sub = i.name
    )
    ELSE 0
  END AS real_on_shelf_count,
  CASE
    WHEN i.material_count = (
      CASE
        WHEN i.level = 1 THEN (
          SELECT COUNT(*) FROM hy_material m
          WHERE m.status = '上架' AND m.industry_big = i.name
        )
        WHEN i.level = 2 THEN (
          SELECT COUNT(*) FROM hy_material m
          WHERE m.status = '上架' AND m.industry_sub = i.name
        )
        ELSE 0
      END
    ) THEN '一致'
    ELSE '不一致(种子假数据)'
  END AS compare_result
FROM hy_industry i
ORDER BY i.level, i.sort, i.id;

-- 6) 若有数据，列出最近 20 条已上架素材明细
SELECT id, title, industry_big, industry_sub, status, addtime
FROM hy_material
WHERE status = '上架'
ORDER BY addtime DESC
LIMIT 20;

-- 7) 可选：把行业表 material_count 同步为真实数量（确认无误后再执行）
-- UPDATE hy_industry i
-- SET material_count = (
--   CASE
--     WHEN i.level = 1 THEN (
--       SELECT COUNT(*) FROM hy_material m
--       WHERE m.status = '上架' AND m.industry_big = i.name
--     )
--     WHEN i.level = 2 THEN (
--       SELECT COUNT(*) FROM hy_material m
--       WHERE m.status = '上架' AND m.industry_sub = i.name
--     )
--     ELSE 0
--   END
-- );
