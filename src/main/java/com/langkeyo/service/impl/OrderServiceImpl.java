package com.langkeyo.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.langkeyo.dto.OrderListItemDTO;
import com.langkeyo.entity.Order;
import com.langkeyo.entity.Product;
import com.langkeyo.mapper.OrderMapper;
import com.langkeyo.mapper.ProductMapper;
import com.langkeyo.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
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

        List<Order> orders = this.list(queryWrapper);

        Set<Long> productIds = new HashSet<>();
        for (Order order : orders) {
            if (order.getProductId() != null) {
                productIds.add(order.getProductId());
            }
        }

        Map<Long, String> productNameMap = new HashMap<>();
        if (!productIds.isEmpty()) {
            List<Product> products = productMapper.selectNameListByIds(List.copyOf(productIds));
            for (Product product : products) {
                productNameMap.put(product.getId(), product.getName());
            }
        }

        return orders.stream()
                .map(order -> toOrderListItemDTO(order, productNameMap))
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
        Map<Long, String> productNameMap = new HashMap<>();
        if (order.getProductId() != null) {
            Product product = productMapper.selectById(order.getProductId());
            if (product != null) {
                productNameMap.put(product.getId(), product.getName());
            }
        }
        return toOrderListItemDTO(order, productNameMap);
    }

    private OrderListItemDTO toOrderListItemDTO(Order order, Map<Long, String> productNameMap) {
        OrderListItemDTO dto = new OrderListItemDTO();
        dto.setId(order.getId());
        dto.setNo(order.getId());

        String productName = productNameMap.get(order.getProductId());
        dto.setName(productName != null ? productName : "商品#" + order.getProductId());

        dto.setQty(1);
        dto.setPrice(order.getTotalPrice() == null ? "0.00" : order.getTotalPrice().toPlainString());
        dto.setStatus(order.getStatus());
        return dto;
    }
}
