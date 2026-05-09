package com.langkeyo.controller;

import com.langkeyo.common.Result;
import com.langkeyo.common.ResultCode;
import com.langkeyo.entity.Order;
import com.langkeyo.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/pay/mock")
public class PayController {

    @Autowired
    private IOrderService orderService;

    @PostMapping("/prepay")
    public Result<Map<String, Object>> prepay(@RequestParam String orderId) {
        Order order = orderService.getById(orderId);
        if (order == null) {
            return Result.error(ResultCode.ORDER_NOT_FOUND);
        }
        if (order.getStatus() != null && order.getStatus() == -1) {
            return Result.error("订单已取消，无法支付");
        }
        if (order.getStatus() != null && (order.getStatus() == 2 || order.getStatus() == 3)) {
            return Result.error("订单已支付，无需重复预下单");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("orderId", order.getId());
        data.put("amount", order.getTotalPrice());
        data.put("payToken", "MOCK_" + UUID.randomUUID().toString().replace("-", ""));
        data.put("expireAt", LocalDateTime.now().plusMinutes(15));
        data.put("message", "模拟预下单成功（演示链路）");
        return Result.success(data);
    }

    @PutMapping("/confirm/{orderId}")
    public Result<String> confirm(@PathVariable String orderId) {
        Order order = orderService.getById(orderId);
        if (order == null) {
            return Result.error(ResultCode.ORDER_NOT_FOUND);
        }
        if (order.getStatus() != null && order.getStatus() == -1) {
            return Result.error("订单已取消，无法支付");
        }
        if (order.getStatus() != null && (order.getStatus() == 2 || order.getStatus() == 3)) {
            return Result.success("订单已支付（演示链路）");
        }
        if (order.getStatus() != null && order.getStatus() != 1) {
            return Result.error(ResultCode.ORDER_STATUS_ERROR);
        }
        boolean ok = orderService.updateOrderStatus(orderId, 2);
        if (!ok) {
            return Result.error(ResultCode.ORDER_UPDATE_FAIL);
        }
        return Result.success("模拟支付成功（演示链路）");
    }
}
