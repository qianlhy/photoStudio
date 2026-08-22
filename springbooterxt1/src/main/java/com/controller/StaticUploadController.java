package com.controller;

import com.annotation.IgnoreAuth;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.File;

/**
 * 静态上传资源（样片视频/封面），免登录，支持 Pad 端 video 组件拉流。
 */
@RestController
public class StaticUploadController {

    @IgnoreAuth
    @GetMapping("/upload/**")
    public ResponseEntity<Resource> serveUpload(HttpServletRequest request) {
        String path = extractUploadPath(request);
        if (!StringUtils.hasText(path) || path.contains("..")) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = resolve(path);
        if (resource == null || !resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .contentType(mediaType(path))
                .body(resource);
    }

    private String extractUploadPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();
        String rel = uri.substring(ctx.length());
        if (rel.startsWith("/")) {
            rel = rel.substring(1);
        }
        return rel;
    }

    private Resource resolve(String path) {
        // JAR 内：classpath:static/upload/...
        ClassPathResource inJar = new ClassPathResource("static/" + path);
        if (inJar.exists()) {
            return inJar;
        }
        // 服务器 JAR 同级目录：static/upload/...（可不重打 JAR 直接放文件）
        File external = new File("static/" + path);
        if (external.exists()) {
            return new FileSystemResource(external);
        }
        File cwd = new File(System.getProperty("user.dir"), "static/" + path);
        if (cwd.exists()) {
            return new FileSystemResource(cwd);
        }
        return null;
    }

    private MediaType mediaType(String path) {
        String lower = path.toLowerCase();
        if (lower.endsWith(".mp4")) {
            return MediaType.parseMediaType("video/mp4");
        }
        if (lower.endsWith(".webm")) {
            return MediaType.parseMediaType("video/webm");
        }
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG;
        }
        if (lower.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}
