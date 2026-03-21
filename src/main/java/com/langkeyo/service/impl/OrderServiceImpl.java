package com.langkeyo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.langkeyo.entity.Order;
import com.langkeyo.entity.Product;
import com.langkeyo.mapper.OrderMapper;
import com.langkeyo.mapper.ProductMapper;
import com.langkeyo.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cn.hutool.core.util.IdUtil;

import java.math.BigDecimal;
import java.util.List;
import com.langkeyo.dto.OrderListItemDTO;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    @Autowired
    private ProductMapper productMapper;

    @Override
    public boolean createOrder(Order order) {
        order.setId(IdUtil.getSnowflakeNextIdStr());
        order.setStatus(1);

        if (order.getUserId() == null) {
            order.setUserId(1L);
        }

        if (order.getProductId() == null) {
            order.setProductId(1L);
        }

        if (order.getPickPointId() == null) {
            order.setPickPointId(1L);
        }

        if (order.getTotalPrice() == null) {
            order.setTotalPrice(new BigDecimal("9.90"));
        }

        return this.save(order);
    }

    @Override
    public List<OrderListItemDTO> getOrdersByUserId(Long userId) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getUserId, userId);
        queryWrapper.orderByDesc(Order::getCreateTime);

        return this.list(queryWrapper)
                .stream()
                .map(this::toOrderListItemDTO)
                .collect(Collectors.toList());
    }

    @Override
    public OrderListItemDTO getOrderDetailById(String orderId) {
        Order order = this.getById(orderId);
        if (order == null) {
            return null;
        }
        return toOrderListItemDTO(order);
    }

    private OrderListItemDTO toOrderListItemDTO(Order order) {
        OrderListItemDTO dto = new OrderListItemDTO();
        dto.setId(order.getId());
        dto.setNo(order.getId());
        Product product = null;
        if (order.getProductId() != null) {
            product = productMapper.selectById(order.getProductId());
        }
        dto.setName(product != null ? product.getName() : "商品#" + order.getProductId());
        dto.setQty(1);
        dto.setPrice(order.getTotalPrice() == null ? "0.00" : order.getTotalPrice().toPlainString());
        dto.setStatus(order.getStatus());
        return dto;
    }
}
