package com.utils;

import java.io.File;

/**
 * 上传文件目录：与 StaticUploadController、静态资源 file:static/ 保持一致。
 */
public final class UploadPathUtils {

    private UploadPathUtils() {
    }

    /** JAR / 进程工作目录下的 static/upload */
    public static File resolveUploadDir() {
        File dir = new File(System.getProperty("user.dir"), "static/upload");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static File resolveUploadFile(String fileName) {
        if (fileName == null || fileName.isEmpty()
                || fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            return null;
        }
        return new File(resolveUploadDir(), fileName);
    }

    /** 历史错误路径：部分环境曾写入 cwd/upload */
    public static File resolveLegacyUploadFile(String fileName) {
        if (fileName == null || fileName.isEmpty()
                || fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            return null;
        }
        return new File(System.getProperty("user.dir"), "upload/" + fileName);
    }

    /** 开发时同步到源码目录，避免 IDE 重启后 target/classes 被清空 */
    public static File resolveDevSourceUploadDir() {
        return ProjectPathUtils.resolveProjectSubDir("src/main/resources/static/upload");
    }
}
