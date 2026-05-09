package com.langkeyo.controller;

import com.langkeyo.common.Result;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/file")
public class FileUploadController {

    private static final long MAX_IMAGE_SIZE = 2L * 1024 * 1024; // 2MB
    private static final Path UPLOAD_DIR = Paths.get("uploads", "products");

    @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("请选择要上传的图片");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("仅支持图片文件上传");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            return Result.error("图片过大，请上传 2MB 以内图片");
        }
        try {
            Files.createDirectories(UPLOAD_DIR);
            String originalName = file.getOriginalFilename();
            String ext = ".jpg";
            if (StringUtils.hasText(originalName) && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
            }
            String filename = UUID.randomUUID().toString().replace("-", "") + ext;
            Path target = UPLOAD_DIR.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            String url = "/uploads/products/" + filename;
            Map<String, String> data = new HashMap<>();
            data.put("url", url);
            data.put("filename", filename);
            return Result.success(data);
        } catch (IOException e) {
            return Result.error("图片上传失败: " + e.getMessage());
        }
    }
}

