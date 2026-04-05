package com.langkeyo.controller;

import com.langkeyo.common.Result;
import com.langkeyo.common.ResultCode;
import com.langkeyo.config.RabbitConfig;
import com.langkeyo.dto.OrderListItemDTO;
import com.langkeyo.entity.Order;
import com.langkeyo.service.IOrderService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/order")
public class OrderController {
    @Autowired
    private IOrderService orderService;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostMapping("/create")
    public Result<String> createOrder(@RequestBody Order order) {
        String lockKey = "lock:order:create:" + order.getUserId() + ":" + order.getProductId();
        RLock lock = redissonClient.getLock(lockKey);

        boolean locked = false;
        try {
            locked = lock.tryLock(0, 5, TimeUnit.SECONDS);
            if (!locked) {
                return Result.error("请勿重复提交");
            }

            boolean success = orderService.createOrder(order);
            if (success) {
                Map<String, Object> msg = new HashMap<>();
                msg.put("orderId", order.getId());
                msg.put("userId", order.getUserId());
                msg.put("productId", order.getProductId());
                msg.put("status", order.getStatus());
                msg.put("time", System.currentTimeMillis());

                rabbitTemplate.convertAndSend(RabbitConfig.ORDER_EVENT_EXCHANGE, RabbitConfig.ORDER_CREATED_ROUTING_KEY, msg);

                return Result.success("订单创建成功");
            }
            return Result.error(ResultCode.ORDER_CREATE_FAIL);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Result.error("系统繁忙，请稍后重试");
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
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

    @PutMapping("/updateStatus/{orderId}")
    public Result<String> updateOrderStatus(@PathVariable String orderId, @RequestParam Integer status) {
        // 先判断订单是否存在
        OrderListItemDTO order = orderService.getOrderDetailById(orderId);
        if (order == null) {
            return Result.error(ResultCode.ORDER_NOT_FOUND);
        }

        // 用当前状态 + 目标状态判断是否合法
        Integer current = order.getStatus();
        boolean legal = (current == 1 && status == 2) || (current == 2 && status == 3);
        if (!legal) {
            return Result.error(ResultCode.ORDER_STATUS_ERROR);
        }

        boolean flag =  orderService.updateOrderStatus(orderId, status);
        if (!flag) {
            return Result.error(ResultCode.ORDER_UPDATE_FAIL);
        }
        return Result.success("订单状态更新成功");
    }
}
