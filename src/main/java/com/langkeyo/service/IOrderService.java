package com.langkeyo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.langkeyo.entity.Order;
import com.langkeyo.dto.OrderListItemDTO;

import java.util.List;

public interface IOrderService extends IService<Order> {
    /**
     * 创建订单
     */
    boolean createOrder(Order order);

    /**
     * 根据用户ID查询订单列表
     */
    List<OrderListItemDTO> getOrdersByUserId(Long userId);

    /**
     * 根据订单ID查询订单详情
     */
    OrderListItemDTO getOrderDetailById(String orderId);
}
