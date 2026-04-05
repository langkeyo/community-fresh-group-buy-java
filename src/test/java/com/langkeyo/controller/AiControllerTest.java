package com.langkeyo.controller;

import cn.hutool.json.JSONUtil;
import com.langkeyo.common.Result;
import com.langkeyo.entity.AiRecipeLibrary;
import com.langkeyo.entity.AiRecommendReview;
import com.langkeyo.mapper.AiRecipeLibraryMapper;
import com.langkeyo.mapper.AiRecommendReviewMapper;
import com.langkeyo.service.AiLlmService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiControllerTest {

    @Mock
    private AiLlmService aiLlmService;
    @Mock
    private AiRecommendReviewMapper aiRecommendReviewMapper;
    @Mock
    private AiRecipeLibraryMapper aiRecipeLibraryMapper;
    @Mock
    private Environment environment;

    @InjectMocks
    private AiController aiController;

    @Test
    void reject_requires_remark_min_length() {
        AiRecommendReview row = new AiRecommendReview();
        row.setId(1L);
        when(aiRecommendReviewMapper.selectById(1L)).thenReturn(row);

        Result<String> result = aiController.reject(1L, "admin", "a");

        assertEquals(500, result.getCode());
        assertEquals("驳回原因至少2个字", result.getMessage());
        verify(aiRecommendReviewMapper, never()).updateById(any());
    }

    @Test
    void test_llm_blocked_when_not_dev_profile() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});

        Result<String> result = aiController.testLlm("hello");

        assertEquals(500, result.getCode());
        assertEquals("该接口仅开发环境可用", result.getMessage());
        verify(aiLlmService, never()).ask(any());
    }

    @Test
    void approve_is_idempotent_when_library_exists() {
        AiRecommendReview row = new AiRecommendReview();
        row.setId(2L);
        row.setStatus("PENDING");
        row.setQueryText("  Foo  BAR ");
        row.setRecipeJson(JSONUtil.createObj().set("title", "T1").toString());
        when(aiRecommendReviewMapper.selectById(2L)).thenReturn(row);
        when(aiRecipeLibraryMapper.selectOne(any())).thenReturn(new AiRecipeLibrary());

        Result<String> result = aiController.approve(2L, "admin");

        assertEquals(200, result.getCode());
        assertEquals("审核通过", result.getData());
        verify(aiRecipeLibraryMapper, never()).insert(any());
    }

    @Test
    void regenerate_only_for_rejected() {
        AiRecommendReview row = new AiRecommendReview();
        row.setId(3L);
        row.setStatus("PENDING");
        when(aiRecommendReviewMapper.selectById(3L)).thenReturn(row);

        Result<String> result = aiController.regenerate(3L, "admin");

        assertEquals(500, result.getCode());
        assertEquals("仅支持驳回记录重新生成", result.getMessage());
        verify(aiRecommendReviewMapper, never()).updateById(any());
    }

    @Test
    void regenerate_sets_pending_and_updates_recipe() {
        AiRecommendReview row = new AiRecommendReview();
        row.setId(4L);
        row.setStatus("REJECTED");
        row.setQueryText("test query");
        when(aiRecommendReviewMapper.selectById(4L)).thenReturn(row);
        when(aiLlmService.askRecipeJson(any())).thenReturn(JSONUtil.createObj()
                .set("title", "T2")
                .set("desc", "D")
                .set("tags", JSONUtil.createArray())
                .set("image", "")
                .set("steps", JSONUtil.createArray()));

        Result<String> result = aiController.regenerate(4L, "admin");

        assertEquals(200, result.getCode());
        assertEquals("已重新生成", result.getData());
        assertEquals("PENDING", row.getStatus());
        verify(aiRecommendReviewMapper, times(1)).updateById(row);
    }
}
