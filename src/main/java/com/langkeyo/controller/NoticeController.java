package com.langkeyo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.langkeyo.common.Result;
import com.langkeyo.common.ResultCode;
import com.langkeyo.dto.NoticeItemDTO;
import com.langkeyo.entity.Notice;
import com.langkeyo.entity.NoticeRead;
import com.langkeyo.mapper.NoticeMapper;
import com.langkeyo.mapper.NoticeReadMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notice")
public class NoticeController {

    @Autowired
    private NoticeMapper noticeMapper;

    @Autowired
    private NoticeReadMapper noticeReadMapper;

    @PostMapping("/admin/create")
    public Result<String> adminCreate(@RequestBody Notice payload) {
        if (!StringUtils.hasText(payload.getTitle()) || !StringUtils.hasText(payload.getContent())) {
            return Result.error(ResultCode.PARAM_IS_BLANK);
        }
        if (!isValidStatus(payload.getStatus())) {
            return Result.error(ResultCode.PARAM_ERROR);
        }
        payload.setId(null);
        payload.setTitle(payload.getTitle().trim());
        payload.setContent(payload.getContent().trim());
        payload.setStatus(payload.getStatus() == null ? 1 : payload.getStatus());
        payload.setDeleted(0);
        payload.setCreateTime(LocalDateTime.now());
        payload.setUpdateTime(LocalDateTime.now());
        boolean ok = noticeMapper.insert(payload) > 0;
        if (!ok) {
            return Result.error("通知发布失败");
        }
        return Result.success("通知发布成功");
    }

    @GetMapping("/admin/list")
    public Result<List<NoticeItemDTO>> adminList(@RequestParam(required = false) Integer status) {
        QueryWrapper<Notice> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", 0);
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("id");
        List<NoticeItemDTO> data = noticeMapper.selectList(wrapper).stream()
                .map(this::toItemDTO)
                .collect(Collectors.toList());
        return Result.success(data);
    }

    @PutMapping("/admin/status/{id}")
    public Result<String> adminUpdateStatus(@PathVariable Long id, @RequestParam Integer status) {
        if (!isValidStatus(status)) {
            return Result.error(ResultCode.PARAM_ERROR);
        }
        Notice db = noticeMapper.selectById(id);
        if (db == null || (db.getDeleted() != null && db.getDeleted() == 1)) {
            return Result.error("通知不存在");
        }
        db.setStatus(status);
        db.setUpdateTime(LocalDateTime.now());
        boolean ok = noticeMapper.updateById(db) > 0;
        if (!ok) {
            return Result.error("通知状态更新失败");
        }
        return Result.success("通知状态更新成功");
    }

    @GetMapping("/list")
    public Result<List<NoticeItemDTO>> list(@RequestParam Long userId) {
        QueryWrapper<Notice> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", 0);
        wrapper.eq("status", 1);
        wrapper.orderByDesc("id");
        List<Notice> notices = noticeMapper.selectList(wrapper);
        if (notices.isEmpty()) {
            return Result.success(List.of());
        }

        List<Long> noticeIds = notices.stream().map(Notice::getId).collect(Collectors.toList());
        QueryWrapper<NoticeRead> readWrapper = new QueryWrapper<>();
        readWrapper.eq("user_id", userId);
        readWrapper.in("notice_id", noticeIds);
        Set<Long> readIds = new HashSet<>(noticeReadMapper.selectList(readWrapper).stream()
                .map(NoticeRead::getNoticeId)
                .collect(Collectors.toSet()));

        List<NoticeItemDTO> data = notices.stream().map(n -> {
            NoticeItemDTO dto = toItemDTO(n);
            dto.setRead(readIds.contains(n.getId()));
            return dto;
        }).collect(Collectors.toList());
        return Result.success(data);
    }

    @PutMapping("/read/{id}")
    public Result<String> read(@PathVariable Long id, @RequestParam Long userId) {
        Notice db = noticeMapper.selectById(id);
        if (db == null || (db.getDeleted() != null && db.getDeleted() == 1) || db.getStatus() == null || db.getStatus() != 1) {
            return Result.error("通知不存在或已下线");
        }
        QueryWrapper<NoticeRead> wrapper = new QueryWrapper<>();
        wrapper.eq("notice_id", id);
        wrapper.eq("user_id", userId);
        NoticeRead existed = noticeReadMapper.selectOne(wrapper);
        if (existed != null) {
            return Result.success("已读状态已更新");
        }
        NoticeRead row = new NoticeRead();
        row.setId(null);
        row.setNoticeId(id);
        row.setUserId(userId);
        row.setReadTime(LocalDateTime.now());
        boolean ok;
        try {
            ok = noticeReadMapper.insert(row) > 0;
        } catch (DuplicateKeyException e) {
            return Result.success("已读状态已更新");
        }
        if (!ok) {
            return Result.error("已读更新失败");
        }
        return Result.success("已读状态更新成功");
    }

    private boolean isValidStatus(Integer status) {
        return status == null || status == 0 || status == 1;
    }

    private NoticeItemDTO toItemDTO(Notice notice) {
        NoticeItemDTO dto = new NoticeItemDTO();
        dto.setId(notice.getId());
        dto.setTitle(notice.getTitle());
        dto.setContent(notice.getContent());
        dto.setStatus(notice.getStatus());
        dto.setRead(false);
        dto.setCreateTime(notice.getCreateTime());
        return dto;
    }
}
