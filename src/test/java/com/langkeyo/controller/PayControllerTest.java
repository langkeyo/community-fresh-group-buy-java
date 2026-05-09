package com.langkeyo.controller;

import com.langkeyo.common.Result;
import com.langkeyo.entity.Order;
import com.langkeyo.service.IOrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayControllerTest {

    @Mock
    private IOrderService orderService;

    @InjectMocks
    private PayController payController;

    @Test
    void confirm_updates_order_to_paid() {
        Order order = new Order();
        order.setId("o1");
        order.setStatus(1);
        when(orderService.getById("o1")).thenReturn(order);
        when(orderService.updateOrderStatus("o1", 2)).thenReturn(true);

        Result<String> result = payController.confirm("o1");

        assertEquals(200, result.getCode());
        assertEquals("模拟支付成功（演示链路）", result.getData());
        verify(orderService).updateOrderStatus("o1", 2);
    }

    @Test
    void confirm_is_idempotent_when_already_paid() {
        Order order = new Order();
        order.setId("o2");
        order.setStatus(2);
        when(orderService.getById("o2")).thenReturn(order);

        Result<String> result = payController.confirm("o2");

        assertEquals(200, result.getCode());
        assertEquals("订单已支付（演示链路）", result.getData());
        verify(orderService, never()).updateOrderStatus("o2", 2);
    }
}
