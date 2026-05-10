package com.langkeyo.controller;

import com.langkeyo.common.Result;
import com.langkeyo.entity.UploadFileRecord;
import com.langkeyo.mapper.UploadFileRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/file")
public class FileUploadController {

    private static final long MAX_FILE_SIZE = 20L * 1024 * 1024; // 20MB
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList("image/jpeg", "image/png", "image/webp", "image/gif");
    private static final Path UPLOAD_ROOT = Paths.get("uploads");

    @Autowired
    private UploadFileRecordMapper uploadFileRecordMapper;

    @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        return upload(file, "product", "admin");
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file,
                                              @RequestParam(defaultValue = "common") String bizType,
                                              @RequestParam(defaultValue = "system") String uploader) {
        if (file == null || file.isEmpty()) return Result.error("请选择要上传的文件");
        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType)) return Result.error("无法识别文件类型");
        if (file.getSize() > MAX_FILE_SIZE) return Result.error("文件过大，请上传 20MB 以内文件");
        if (bizType.equals("product") && !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            return Result.error("商品图片仅支持 jpg/png/webp/gif");
        }
        String safeBiz = sanitizeBizType(bizType);
        try {
            Path uploadDir = UPLOAD_ROOT.resolve(safeBiz);
            Files.createDirectories(uploadDir);
            String originalName = file.getOriginalFilename();
            String ext = ".bin";
            if (StringUtils.hasText(originalName) && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
            }
            String filename = UUID.randomUUID().toString().replace("-", "") + ext;
            Path target = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            String url = "/uploads/" + safeBiz + "/" + filename;

            UploadFileRecord row = new UploadFileRecord();
            row.setBizType(safeBiz);
            row.setOriginName(StringUtils.hasText(originalName) ? originalName : filename);
            row.setStoredName(filename);
            row.setContentType(contentType);
            row.setSizeBytes(file.getSize());
            row.setUrl(url);
            row.setUploadedBy(uploader);
            row.setDeleted(0);
            row.setCreateTime(LocalDateTime.now());
            row.setUpdateTime(LocalDateTime.now());
            uploadFileRecordMapper.insert(row);

            Map<String, String> data = new HashMap<>();
            data.put("url", url);
            data.put("filename", filename);
            data.put("bizType", safeBiz);
            data.put("id", String.valueOf(row.getId()));
            return Result.success(data);
        } catch (IOException e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    @GetMapping("/admin/list")
    public Result<List<UploadFileRecord>> adminList(@RequestParam(required = false) String bizType) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UploadFileRecord> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        wrapper.eq("deleted", 0);
        if (StringUtils.hasText(bizType)) {
            wrapper.eq("biz_type", sanitizeBizType(bizType));
        }
        wrapper.orderByDesc("id");
        return Result.success(uploadFileRecordMapper.selectList(wrapper));
    }

    @DeleteMapping("/admin/{id}")
    public Result<String> adminDelete(@PathVariable Long id) {
        UploadFileRecord db = uploadFileRecordMapper.selectById(id);
        if (db == null || (db.getDeleted() != null && db.getDeleted() == 1)) {
            return Result.error("文件记录不存在");
        }
        try {
            Path target = UPLOAD_ROOT.resolve(db.getBizType()).resolve(db.getStoredName());
            Files.deleteIfExists(target);
        } catch (IOException ignored) {
            // ignore file deletion errors, still remove db record for consistency
        }
        uploadFileRecordMapper.deleteById(id);
        return Result.success("文件删除成功");
    }

    private String sanitizeBizType(String bizType) {
        if (!StringUtils.hasText(bizType)) return "common";
        return bizType.trim().toLowerCase().replaceAll("[^a-z0-9_-]", "");
    }
}
