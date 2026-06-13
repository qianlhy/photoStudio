package com.utils;

import java.io.File;

/**
 * 解析项目根目录下的外部资源路径（admin、front 等已移出 resources 的目录）
 */
public class ProjectPathUtils {

    private ProjectPathUtils() {
    }

    /**
     * 返回 Spring ResourceHandler 可用的 file: 路径，末尾带 /
     */
    public static String externalDirLocation(String dirName) {
        File dir = resolveProjectSubDir(dirName);
        String uri = dir.getAbsoluteFile().toURI().toString();
        return uri.endsWith("/") ? uri : uri + "/";
    }

    /**
     * 从 user.dir 向上查找，定位项目根目录下的子目录
     */
    public static File resolveProjectSubDir(String relativePath) {
        File baseDir = new File(System.getProperty("user.dir"));
        File target = new File(baseDir, relativePath);
        if (target.exists()) {
            return target;
        }
        File current = baseDir;
        for (int i = 0; i < 5 && current != null; i++) {
            File candidate = new File(current, relativePath);
            if (candidate.exists()) {
                return candidate;
            }
            current = current.getParentFile();
        }
        return target;
    }
}
