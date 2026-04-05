package com.langkeyo.listener;

import com.langkeyo.config.RabbitConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class OrderCreatedListener {
    @RabbitListener(queues = RabbitConfig.ORDER_CREATED_QUEUE)
    public void onOrderCreated(Map<String, Object> msg) {
        log.info("[ORDER_CREATED] 收到订单创建消息：{}", msg);
        log.info("[ORDER_CREATED] 模拟异步通知: 订单{}已创建，后续可发送短信/站内信", msg.get("orderId"));
    }
}
