package com.langkeyo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.langkeyo.dto.LeaderWorkbenchDTO;
import com.langkeyo.dto.OpenGroupItemDTO;
import com.langkeyo.entity.Order;
import com.langkeyo.dto.OrderListItemDTO;

import java.util.List;
import java.util.Optional;

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
     * 管理端：查询全部订单，可按状态/自提点/时间范围筛选
     */
    List<OrderListItemDTO> getAllOrders(Integer status, Long pickPointId, String startTime, String endTime);

    /**
     * 团长侧：按自提点与状态筛选订单
     */
    List<OrderListItemDTO> getOrdersByPickPointAndStatus(Long pickPointId, Integer status);

    /**
     * 团长工作台：待核销数、今日核销数、最近核销记录
     */
    LeaderWorkbenchDTO getLeaderWorkbench(Long pickPointId);

    /**
     * 更新订单状态
     */
    boolean updateOrderStatus(String orderId, Integer status);

    /**
     * 查询可加入的拼团列表
     */
    List<OpenGroupItemDTO> listOpenGroups(Long productId, Long pickPointId);

    /**
     * 查询拼团状态（仅进行中）
     */
    Optional<OpenGroupItemDTO> getOpenGroup(String groupBuyId, Long productId, Long pickPointId);
}
