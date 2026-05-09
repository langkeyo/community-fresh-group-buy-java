package com.langkeyo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.langkeyo.common.Result;
import com.langkeyo.common.ResultCode;
import com.langkeyo.dto.SupportTicketItemDTO;
import com.langkeyo.entity.SupportTicket;
import com.langkeyo.mapper.SupportTicketMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/support")
public class SupportTicketController {

    @Autowired
    private SupportTicketMapper supportTicketMapper;

    @PostMapping("/ticket")
    public Result<String> create(@RequestBody SupportTicket payload) {
        if (payload.getUserId() == null || !StringUtils.hasText(payload.getTitle()) || !StringUtils.hasText(payload.getContent())) {
            return Result.error(ResultCode.PARAM_IS_BLANK);
        }
        SupportTicket row = new SupportTicket();
        row.setId(null);
        row.setUserId(payload.getUserId());
        row.setTitle(payload.getTitle().trim());
        row.setContent(payload.getContent().trim());
        row.setContact(StringUtils.hasText(payload.getContact()) ? payload.getContact().trim() : "");
        row.setStatus(1);
        row.setDeleted(0);
        row.setCreateTime(LocalDateTime.now());
        row.setUpdateTime(LocalDateTime.now());
        boolean ok = supportTicketMapper.insert(row) > 0;
        if (!ok) {
            return Result.error("工单提交失败");
        }
        return Result.success("工单提交成功");
    }

    @GetMapping("/my-list")
    public Result<List<SupportTicketItemDTO>> myList(@RequestParam Long userId) {
        QueryWrapper<SupportTicket> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", 0);
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("id");
        List<SupportTicketItemDTO> data = supportTicketMapper.selectList(wrapper).stream()
                .map(this::toItemDTO)
                .collect(Collectors.toList());
        return Result.success(data);
    }

    @GetMapping("/admin/list")
    public Result<List<SupportTicketItemDTO>> adminList(@RequestParam(required = false) Integer status) {
        QueryWrapper<SupportTicket> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", 0);
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("id");
        List<SupportTicketItemDTO> data = supportTicketMapper.selectList(wrapper).stream()
                .map(this::toItemDTO)
                .collect(Collectors.toList());
        return Result.success(data);
    }

    @PutMapping("/admin/reply/{id}")
    public Result<String> adminReply(@PathVariable Long id,
                                     @RequestParam String operator,
                                     @RequestParam String replyContent) {
        if (!StringUtils.hasText(operator) || !StringUtils.hasText(replyContent)) {
            return Result.error(ResultCode.PARAM_IS_BLANK);
        }
        SupportTicket db = supportTicketMapper.selectById(id);
        if (db == null || (db.getDeleted() != null && db.getDeleted() == 1)) {
            return Result.error("工单不存在");
        }
        if (db.getStatus() != null && db.getStatus() == 3) {
            return Result.error("工单已关闭，无法回复");
        }
        if (db.getStatus() != null && db.getStatus() == 2) {
            return Result.error("工单已回复，禁止重复回复");
        }
        db.setReplyBy(operator.trim());
        db.setReplyContent(replyContent.trim());
        db.setReplyTime(LocalDateTime.now());
        db.setStatus(2);
        db.setUpdateTime(LocalDateTime.now());
        boolean ok = supportTicketMapper.updateById(db) > 0;
        if (!ok) {
            return Result.error("工单回复失败");
        }
        return Result.success("工单回复成功");
    }

    private SupportTicketItemDTO toItemDTO(SupportTicket row) {
        SupportTicketItemDTO dto = new SupportTicketItemDTO();
        dto.setId(row.getId());
        dto.setUserId(row.getUserId());
        dto.setTitle(row.getTitle());
        dto.setContent(row.getContent());
        dto.setContact(row.getContact());
        dto.setStatus(row.getStatus());
        dto.setReplyContent(row.getReplyContent());
        dto.setReplyBy(row.getReplyBy());
        dto.setReplyTime(row.getReplyTime());
        dto.setCreateTime(row.getCreateTime());
        return dto;
    }
}
