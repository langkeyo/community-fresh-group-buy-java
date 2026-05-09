package com.langkeyo.controller;

import com.langkeyo.common.Result;
import com.langkeyo.entity.Notice;
import com.langkeyo.entity.NoticeRead;
import com.langkeyo.mapper.NoticeMapper;
import com.langkeyo.mapper.NoticeReadMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoticeControllerTest {

    @Mock
    private NoticeMapper noticeMapper;
    @Mock
    private NoticeReadMapper noticeReadMapper;

    @InjectMocks
    private NoticeController noticeController;

    @Test
    void admin_create_rejects_invalid_status() {
        Notice payload = new Notice();
        payload.setTitle("t");
        payload.setContent("c");
        payload.setStatus(9);

        Result<String> result = noticeController.adminCreate(payload);

        assertEquals(4001, result.getCode());
        verify(noticeMapper, never()).insert(any());
    }

    @Test
    void admin_update_status_rejects_invalid_status() {
        Result<String> result = noticeController.adminUpdateStatus(1L, 9);

        assertEquals(4001, result.getCode());
        verify(noticeMapper, never()).updateById(any());
    }

    @Test
    void read_is_idempotent_on_duplicate_insert() {
        Notice db = new Notice();
        db.setId(1L);
        db.setStatus(1);
        db.setDeleted(0);
        when(noticeMapper.selectById(1L)).thenReturn(db);
        when(noticeReadMapper.selectOne(any())).thenReturn(null);
        when(noticeReadMapper.insert(any(NoticeRead.class))).thenThrow(new DuplicateKeyException("dup"));

        Result<String> result = noticeController.read(1L, 100L);

        assertEquals(200, result.getCode());
        assertEquals("已读状态已更新", result.getData());
    }
}
