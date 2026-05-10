package com.langkeyo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.langkeyo.common.Result;
import com.langkeyo.common.ResultCode;
import com.langkeyo.dto.SupportMessageItemDTO;
import com.langkeyo.dto.SupportReplyReqDTO;
import com.langkeyo.dto.SupportTransferReqDTO;
import com.langkeyo.dto.SupportActionLogItemDTO;
import com.langkeyo.dto.SupportTicketItemDTO;
import com.langkeyo.entity.SupportActionLog;
import com.langkeyo.entity.SupportMessage;
import com.langkeyo.entity.SupportTicket;
import com.langkeyo.mapper.SupportActionLogMapper;
import com.langkeyo.mapper.SupportMessageMapper;
import com.langkeyo.mapper.SupportTicketMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/support")
public class SupportTicketController {

    @Autowired
    private SupportTicketMapper supportTicketMapper;
    @Autowired
    private SupportMessageMapper supportMessageMapper;
    @Autowired
    private SupportActionLogMapper supportActionLogMapper;

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
        row.setAssignedTo("");
        row.setClosedBy("");
        row.setClosedTime(null);
        row.setUnreadAdminCount(1);
        row.setUnreadUserCount(0);
        row.setLastMessageTime(LocalDateTime.now());
        row.setDeleted(0);
        row.setCreateTime(LocalDateTime.now());
        row.setUpdateTime(LocalDateTime.now());
        boolean ok = supportTicketMapper.insert(row) > 0;
        if (!ok) {
            return Result.error("工单提交失败");
        }
        SupportMessage firstMessage = new SupportMessage();
        firstMessage.setTicketId(row.getId());
        firstMessage.setSenderType("USER");
        firstMessage.setSenderId(row.getUserId());
        firstMessage.setSenderName("用户#" + row.getUserId());
        firstMessage.setContent(row.getContent());
        firstMessage.setDeleted(0);
        firstMessage.setCreateTime(LocalDateTime.now());
        firstMessage.setUpdateTime(LocalDateTime.now());
        supportMessageMapper.insert(firstMessage);
        writeActionLog(row.getId(), "CREATE", "USER#" + row.getUserId(), "用户提交工单");
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

    @GetMapping("/messages/{ticketId}")
    public Result<List<SupportMessageItemDTO>> ticketMessages(@PathVariable Long ticketId,
                                                              @RequestParam Long userId,
                                                              @RequestParam(required = false) Long sinceId) {
        SupportTicket db = supportTicketMapper.selectById(ticketId);
        if (db == null || (db.getDeleted() != null && db.getDeleted() == 1)) {
            return Result.error("工单不存在");
        }
        if (!db.getUserId().equals(userId)) {
            return Result.error("无权访问该会话");
        }
        QueryWrapper<SupportMessage> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", 0).eq("ticket_id", ticketId);
        if (sinceId != null && sinceId > 0) {
            wrapper.gt("id", sinceId);
        }
        wrapper.orderByAsc("id");
        List<SupportMessageItemDTO> data = supportMessageMapper.selectList(wrapper).stream()
                .map(this::toMessageDTO)
                .collect(Collectors.toList());
        db.setUnreadUserCount(0);
        db.setUpdateTime(LocalDateTime.now());
        supportTicketMapper.updateById(db);
        return Result.success(data);
    }

    @PostMapping("/message/{ticketId}")
    public Result<String> userSendMessage(@PathVariable Long ticketId,
                                          @RequestParam Long userId,
                                          @RequestParam String content) {
        if (!StringUtils.hasText(content)) {
            return Result.error(ResultCode.PARAM_IS_BLANK);
        }
        SupportTicket db = supportTicketMapper.selectById(ticketId);
        if (db == null || (db.getDeleted() != null && db.getDeleted() == 1)) {
            return Result.error("工单不存在");
        }
        if (!db.getUserId().equals(userId)) {
            return Result.error("无权发送消息");
        }
        if (db.getStatus() != null && db.getStatus() == 3) {
            return Result.error("工单已关闭");
        }
        SupportMessage msg = new SupportMessage();
        msg.setTicketId(ticketId);
        msg.setSenderType("USER");
        msg.setSenderId(userId);
        msg.setSenderName("用户#" + userId);
        msg.setContent(content.trim());
        msg.setDeleted(0);
        msg.setCreateTime(LocalDateTime.now());
        msg.setUpdateTime(LocalDateTime.now());
        supportMessageMapper.insert(msg);
        db.setStatus(1);
        db.setUnreadAdminCount((db.getUnreadAdminCount() == null ? 0 : db.getUnreadAdminCount()) + 1);
        db.setLastMessageTime(LocalDateTime.now());
        db.setUpdateTime(LocalDateTime.now());
        supportTicketMapper.updateById(db);
        return Result.success("发送成功");
    }

    @GetMapping("/admin/list")
    public Result<List<SupportTicketItemDTO>> adminList(@RequestParam(required = false) Integer status,
                                                        @RequestParam(required = false) Boolean mineOnly,
                                                        @RequestParam(required = false) String operator) {
        QueryWrapper<SupportTicket> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", 0);
        if (status != null) {
            wrapper.eq("status", status);
        }
        if (Boolean.TRUE.equals(mineOnly) && StringUtils.hasText(operator)) {
            wrapper.eq("assigned_to", operator.trim());
        }
        wrapper.orderByDesc("id");
        List<SupportTicketItemDTO> data = supportTicketMapper.selectList(wrapper).stream()
                .map(this::toItemDTO)
                .collect(Collectors.toList());
        return Result.success(data);
    }

    @GetMapping("/admin/messages/{ticketId}")
    public Result<List<SupportMessageItemDTO>> adminMessages(@PathVariable Long ticketId,
                                                             @RequestParam(required = false) Long sinceId) {
        SupportTicket db = supportTicketMapper.selectById(ticketId);
        if (db == null || (db.getDeleted() != null && db.getDeleted() == 1)) {
            return Result.error("工单不存在");
        }
        QueryWrapper<SupportMessage> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", 0).eq("ticket_id", ticketId);
        if (sinceId != null && sinceId > 0) {
            wrapper.gt("id", sinceId);
        }
        wrapper.orderByAsc("id");
        List<SupportMessageItemDTO> data = supportMessageMapper.selectList(wrapper).stream()
                .map(this::toMessageDTO)
                .collect(Collectors.toList());
        db.setUnreadAdminCount(0);
        db.setUpdateTime(LocalDateTime.now());
        supportTicketMapper.updateById(db);
        return Result.success(data);
    }

    @PostMapping("/admin/reply/{id}")
    public Result<String> adminReplyByBody(@PathVariable Long id,
                                           @RequestBody SupportReplyReqDTO req) {
        if (req == null) {
            return Result.error(ResultCode.PARAM_IS_BLANK);
        }
        return doAdminReply(id, req.getOperator(), req.getContent());
    }

    @PutMapping("/admin/reply/{id}")
    public Result<String> adminReply(@PathVariable Long id,
                                     @RequestParam String operator,
                                     @RequestParam String replyContent) {
        return doAdminReply(id, operator, replyContent);
    }

    @PutMapping("/admin/assign/{id}")
    public Result<String> adminAssign(@PathVariable Long id, @RequestParam String operator) {
        if (!StringUtils.hasText(operator)) {
            return Result.error(ResultCode.PARAM_IS_BLANK);
        }
        SupportTicket db = supportTicketMapper.selectById(id);
        if (db == null || (db.getDeleted() != null && db.getDeleted() == 1)) {
            return Result.error("工单不存在");
        }
        String from = db.getAssignedTo();
        db.setAssignedTo(operator.trim());
        if (db.getStatus() != null && db.getStatus() == 1) {
            db.setStatus(2);
        }
        db.setUpdateTime(LocalDateTime.now());
        supportTicketMapper.updateById(db);
        writeActionLog(id, "ASSIGN", operator.trim(), "会话分配: " + (StringUtils.hasText(from) ? from : "未分配") + " -> " + operator.trim());
        return Result.success("接单成功");
    }

    @PostMapping("/admin/transfer/{id}")
    public Result<String> adminTransfer(@PathVariable Long id, @RequestBody SupportTransferReqDTO req) {
        if (req == null || !StringUtils.hasText(req.getOperator()) || !StringUtils.hasText(req.getTargetOperator())) {
            return Result.error(ResultCode.PARAM_IS_BLANK);
        }
        SupportTicket db = supportTicketMapper.selectById(id);
        if (db == null || (db.getDeleted() != null && db.getDeleted() == 1)) {
            return Result.error("工单不存在");
        }
        if (db.getStatus() != null && db.getStatus() == 3) {
            return Result.error("工单已关闭，无法转派");
        }
        if (StringUtils.hasText(db.getAssignedTo())
                && !db.getAssignedTo().equalsIgnoreCase(req.getOperator().trim())) {
            return Result.error("仅当前接单客服可转派");
        }
        String from = StringUtils.hasText(db.getAssignedTo()) ? db.getAssignedTo() : "未分配";
        db.setAssignedTo(req.getTargetOperator().trim());
        db.setStatus(2);
        db.setUpdateTime(LocalDateTime.now());
        supportTicketMapper.updateById(db);
        String reason = StringUtils.hasText(req.getReason()) ? ("，原因：" + req.getReason().trim()) : "";
        writeActionLog(id, "TRANSFER", req.getOperator().trim(),
                "会话转派: " + from + " -> " + req.getTargetOperator().trim() + reason);
        return Result.success("转派成功");
    }

    @PutMapping("/admin/close/{id}")
    public Result<String> adminClose(@PathVariable Long id, @RequestParam String operator) {
        if (!StringUtils.hasText(operator)) {
            return Result.error(ResultCode.PARAM_IS_BLANK);
        }
        SupportTicket db = supportTicketMapper.selectById(id);
        if (db == null || (db.getDeleted() != null && db.getDeleted() == 1)) {
            return Result.error("工单不存在");
        }
        db.setStatus(3);
        db.setClosedBy(operator.trim());
        db.setClosedTime(LocalDateTime.now());
        db.setUpdateTime(LocalDateTime.now());
        supportTicketMapper.updateById(db);
        writeActionLog(id, "CLOSE", operator.trim(), "工单关闭");
        return Result.success("工单已关闭");
    }

    @GetMapping("/admin/logs/{ticketId}")
    public Result<List<SupportActionLogItemDTO>> adminLogs(@PathVariable Long ticketId) {
        QueryWrapper<SupportActionLog> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", 0).eq("ticket_id", ticketId).orderByAsc("id");
        List<SupportActionLogItemDTO> data = supportActionLogMapper.selectList(wrapper).stream()
                .map(this::toActionLogDTO)
                .collect(Collectors.toList());
        return Result.success(data);
    }

    @GetMapping("/admin/unread-count")
    public Result<Map<String, Integer>> adminUnreadCount() {
        QueryWrapper<SupportTicket> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", 0);
        List<SupportTicket> rows = supportTicketMapper.selectList(wrapper);
        int count = rows.stream().mapToInt(r -> r.getUnreadAdminCount() == null ? 0 : r.getUnreadAdminCount()).sum();
        Map<String, Integer> data = new HashMap<>();
        data.put("unreadCount", count);
        return Result.success(data);
    }

    private Result<String> doAdminReply(Long id, String operator, String replyContent) {
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
        if (StringUtils.hasText(db.getAssignedTo())
                && !db.getAssignedTo().equalsIgnoreCase(operator.trim())) {
            return Result.error("该会话已分配给 " + db.getAssignedTo());
        }
        if (!StringUtils.hasText(db.getAssignedTo())) {
            db.setAssignedTo(operator.trim());
        }
        db.setReplyBy(operator.trim());
        db.setReplyContent(replyContent.trim());
        db.setReplyTime(LocalDateTime.now());
        db.setStatus(2);
        db.setUnreadUserCount((db.getUnreadUserCount() == null ? 0 : db.getUnreadUserCount()) + 1);
        db.setUnreadAdminCount(0);
        db.setLastMessageTime(LocalDateTime.now());
        db.setUpdateTime(LocalDateTime.now());
        boolean ok = supportTicketMapper.updateById(db) > 0;
        if (!ok) {
            return Result.error("工单回复失败");
        }
        SupportMessage msg = new SupportMessage();
        msg.setTicketId(id);
        msg.setSenderType("ADMIN");
        msg.setSenderId(0L);
        msg.setSenderName(operator.trim());
        msg.setContent(replyContent.trim());
        msg.setDeleted(0);
        msg.setCreateTime(LocalDateTime.now());
        msg.setUpdateTime(LocalDateTime.now());
        supportMessageMapper.insert(msg);
        writeActionLog(id, "REPLY", operator.trim(), "客服回复消息");
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
        dto.setAssignedTo(row.getAssignedTo());
        dto.setClosedBy(row.getClosedBy());
        dto.setClosedTime(row.getClosedTime());
        dto.setLastMessageTime(row.getLastMessageTime());
        dto.setUnreadAdminCount(row.getUnreadAdminCount() == null ? 0 : row.getUnreadAdminCount());
        dto.setUnreadUserCount(row.getUnreadUserCount() == null ? 0 : row.getUnreadUserCount());
        dto.setCreateTime(row.getCreateTime());
        return dto;
    }

    private SupportMessageItemDTO toMessageDTO(SupportMessage row) {
        SupportMessageItemDTO dto = new SupportMessageItemDTO();
        dto.setId(row.getId());
        dto.setTicketId(row.getTicketId());
        dto.setSenderType(row.getSenderType());
        dto.setSenderId(row.getSenderId());
        dto.setSenderName(row.getSenderName());
        dto.setContent(row.getContent());
        dto.setCreateTime(row.getCreateTime());
        return dto;
    }

    private SupportActionLogItemDTO toActionLogDTO(SupportActionLog row) {
        SupportActionLogItemDTO dto = new SupportActionLogItemDTO();
        dto.setId(row.getId());
        dto.setTicketId(row.getTicketId());
        dto.setActionType(row.getActionType());
        dto.setOperator(row.getOperator());
        dto.setDetail(row.getDetail());
        dto.setCreateTime(row.getCreateTime());
        return dto;
    }

    private void writeActionLog(Long ticketId, String actionType, String operator, String detail) {
        SupportActionLog log = new SupportActionLog();
        log.setTicketId(ticketId);
        log.setActionType(actionType);
        log.setOperator(operator);
        log.setDetail(detail);
        log.setDeleted(0);
        log.setCreateTime(LocalDateTime.now());
        log.setUpdateTime(LocalDateTime.now());
        supportActionLogMapper.insert(log);
    }
}
