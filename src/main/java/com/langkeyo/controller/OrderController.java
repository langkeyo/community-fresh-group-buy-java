package com.langkeyo.controller;

import com.langkeyo.common.Result;
import com.langkeyo.common.ResultCode;
import com.langkeyo.dto.OrderListItemDTO;
import com.langkeyo.entity.Order;
import com.langkeyo.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
public class OrderController {
    @Autowired
    private IOrderService orderService;

    @PostMapping("/create")
    public Result<String> createOrder(@RequestBody Order order) {
        boolean success = orderService.createOrder(order);
        if (success) {
            return Result.success("订单创建成功");
        }
        return Result.error(ResultCode.ORDER_CREATE_FAIL);
    }

    @GetMapping("/list/{userId}")
    public Result<List<OrderListItemDTO>> getOrdersByUserId(@PathVariable Long userId) {
        List<OrderListItemDTO> orders = orderService.getOrdersByUserId(userId);
        return Result.success(orders);
    }

    @GetMapping("/{orderId}")
    public Result<OrderListItemDTO> getOrderDetailById(@PathVariable String orderId) {
        OrderListItemDTO order = orderService.getOrderDetailById(orderId);
        if (order == null) {
            return Result.error(ResultCode.ORDER_NOT_FOUND);
        }
        return Result.success(order);
    }
}
