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

    /**
     * 管理端：查询全部订单，可按状态筛选
     */
    List<OrderListItemDTO> getAllOrders(Integer status);

    /**
     * 团长侧：按自提点与状态筛选订单
     */
    List<OrderListItemDTO> getOrdersByPickPointAndStatus(Long pickPointId, Integer status);

    /**
     * 更新订单状态
     */
    boolean updateOrderStatus(String orderId, Integer status);
}
