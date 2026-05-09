package com.langkeyo.controller;

import com.langkeyo.common.Result;
import com.langkeyo.entity.SupportTicket;
import com.langkeyo.mapper.SupportTicketMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupportTicketControllerTest {

    @Mock
    private SupportTicketMapper supportTicketMapper;

    @InjectMocks
    private SupportTicketController supportTicketController;

    @Test
    void admin_reply_rejects_closed_ticket() {
        SupportTicket db = new SupportTicket();
        db.setId(1L);
        db.setStatus(3);
        when(supportTicketMapper.selectById(1L)).thenReturn(db);

        Result<String> result = supportTicketController.adminReply(1L, "admin", "reply");

        assertEquals(500, result.getCode());
        assertEquals("工单已关闭，无法回复", result.getMessage());
        verify(supportTicketMapper, never()).updateById(any());
    }

    @Test
    void admin_reply_rejects_already_replied_ticket() {
        SupportTicket db = new SupportTicket();
        db.setId(2L);
        db.setStatus(2);
        when(supportTicketMapper.selectById(2L)).thenReturn(db);

        Result<String> result = supportTicketController.adminReply(2L, "admin", "reply");

        assertEquals(500, result.getCode());
        assertEquals("工单已回复，禁止重复回复", result.getMessage());
        verify(supportTicketMapper, never()).updateById(any());
    }
}
