package com.langkeyo.service;

import com.langkeyo.config.DeepSeekProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AiLlmServiceTest {

    @Test
    void ask_throws_when_api_key_missing() {
        DeepSeekProperties props = new DeepSeekProperties();
        props.setApiKey("");
        AiLlmService service = new AiLlmService(props);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.ask("hi"));

        assertEquals("AI服务未配置，请联系管理员", ex.getMessage());
    }
}
