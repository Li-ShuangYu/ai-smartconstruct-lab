package com.smartconstruct.backend_core.controller;

import com.smartconstruct.backend_core.dto.ApiResult;
import com.smartconstruct.backend_core.entity.SysResource;
import com.smartconstruct.backend_core.entity.SysUser;
import com.smartconstruct.backend_core.service.ISysResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/file")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final ISysResourceService resourceService;

    public FileController(ISysResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            log.error("无法创建上传目录: {}", uploadDir, e);
        }
    }

    private Long getCurrentUserId() {
        SysUser user = (SysUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getId();
    }

    private String sha256Hex(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data);
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/upload")
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return ApiResult.error("上传文件为空");

        String origName = file.getOriginalFilename();
        if (origName == null) origName = "unknown";

        // 文件类型检测
        String ext = "";
        int dot = origName.lastIndexOf('.');
        if (dot > 0) ext = origName.substring(dot).toLowerCase();

        // 文档类型
        Set<String> docTypes = new HashSet<>(Arrays.asList(".pdf", ".txt", ".doc", ".docx", ".ppt", ".pptx", ".md"));
        // 视频类型
        Set<String> videoTypes = new HashSet<>(Arrays.asList(".wmv", ".mp4", ".m4v", ".mov", ".avi", ".mkv", ".mp3"));

        int resourceType;
        if (docTypes.contains(ext)) {
            resourceType = 3; // 文档
        } else if (videoTypes.contains(ext)) {
            resourceType = 4; // 视频
        } else {
            resourceType = 5; // 其他
        }

        long fileSize = file.getSize();

        // 读取文件内容计算哈希
        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException e) {
            return ApiResult.error("文件读取失败");
        }
        String fileHash = sha256Hex(content);

        // 保存文件到磁盘
        String storeName = fileHash + "_" + origName;
        Path targetPath = Paths.get(uploadDir, storeName);
        try {
            Files.write(targetPath, content);
        } catch (IOException e) {
            log.error("文件写入失败: {}", targetPath, e);
            return ApiResult.error("文件保存失败");
        }

        Long userId = getCurrentUserId();
        LocalDateTime now = LocalDateTime.now();

        SysResource resource = new SysResource();
        resource.setResourceType(resourceType);
        resource.setResourceName(origName);
        resource.setFileUrl("/uploads/" + storeName);
        resource.setFileSize(fileSize);
        resource.setFileHash(fileHash);
        resource.setUploaderId(userId);
        resource.setCreatedAt(now);
        resource.setUpdatedAt(now);
        resourceService.save(resource);

        String sizeStr;
        if (fileSize < 1024) sizeStr = fileSize + "B";
        else if (fileSize < 1024 * 1024) sizeStr = String.format("%.1fKB", fileSize / 1024.0);
        else sizeStr = String.format("%.1fMB", fileSize / (1024.0 * 1024.0));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", String.valueOf(resource.getId()));
        result.put("fileName", origName);
        result.put("fileSize", sizeStr);
        result.put("resourceType", resourceType);

        return ApiResult.ok(result);
    }

    @PostMapping("/upload-video")
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Map<String, Object>> uploadVideo(@RequestParam("file") MultipartFile file) {
        ApiResult<Map<String, Object>> result = upload(file);
        return result;
    }
}