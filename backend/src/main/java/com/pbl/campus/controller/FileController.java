package com.pbl.campus.controller;

import com.pbl.campus.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api")
public class FileController {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        // 校验文件类型（仅图片）
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("仅支持上传图片文件");
        }

        // 校验文件大小（最大 2MB）
        if (file.getSize() > 2 * 1024 * 1024) {
            return Result.error("文件大小不能超过 2MB");
        }

        try {
            // 确保上传目录存在
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + extension;

            // 保存文件
            Path filePath = uploadPath.resolve(newFilename);
            file.transferTo(filePath.toFile());

            // 返回可访问的 URL
            String fileUrl = "/uploads/" + newFilename;
            log.info("文件上传成功: {}", fileUrl);
            return Result.success("上传成功", fileUrl);

        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error("上传失败，请重试");
        }
    }
}
