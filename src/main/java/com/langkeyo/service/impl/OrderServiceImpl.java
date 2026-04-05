package com.langkeyo.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.langkeyo.dto.OrderListItemDTO;
import com.langkeyo.entity.Order;
import com.langkeyo.entity.PickPoint;
import com.langkeyo.entity.Product;
import com.langkeyo.mapper.OrderMapper;
import com.langkeyo.mapper.PickPointMapper;
import com.langkeyo.mapper.ProductMapper;
import com.langkeyo.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private PickPointMapper pickPointMapper;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional
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
            order.setTotalPrice(BigDecimal.ZERO);
        }

        if (order.getProductId() != null) {
            int updated = productMapper.decreaseStock(order.getProductId());
            if (updated == 0) {
                return false;
            }
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

        Map<Long, String> productNameMap = new HashMap<>();
        if (order.getProductId() != null) {
            Product product = productMapper.selectNameById(order.getProductId());
            if (product != null) {
                productNameMap.put(product.getId(), product.getName());
            }
        }

        return toOrderListItemDTO(order, productNameMap);
    }

    @Override
    public List<OrderListItemDTO> getAllOrders(Integer status) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            queryWrapper.eq(Order::getStatus, status);
        }
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
    public List<OrderListItemDTO> getOrdersByPickPointAndStatus(Long pickPointId, Integer status) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getPickPointId, pickPointId);
        if (status != null) {
            queryWrapper.eq(Order::getStatus, status);
        }
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
    public boolean updateOrderStatus(String orderId, Integer status) {
        Order order = this.getById(orderId);
        if (order == null || status == null) {
            return false;
        }

        Integer current = order.getStatus();
        boolean valid = (current == 1 && status == 2) || (current == 2 && status == 3);

        if (!valid) {
            return false;
        }

        return baseMapper.updateOrderStatus(orderId, status) > 0;
    }

    private OrderListItemDTO toOrderListItemDTO(Order order, Map<Long, String> productNameMap) {
        OrderListItemDTO dto = new OrderListItemDTO();
        dto.setId(order.getId());
        dto.setNo(order.getId());
        dto.setProductId(order.getProductId());

        String productName = productNameMap.get(order.getProductId());
        dto.setName(productName != null ? productName : "商品#" + order.getProductId());

        dto.setQty(1);
        dto.setPrice(order.getTotalPrice() == null ? "0.00" : order.getTotalPrice().toPlainString());
        dto.setStatus(order.getStatus());
        String pickPointName = "";
        String pickPointAddress = "";
        if (order.getPickPointId() != null) {
            PickPoint pickPoint = pickPointMapper.selectById(order.getPickPointId());
            if (pickPoint != null) {
                pickPointName = pickPoint.getName();
                pickPointAddress = pickPoint.getAddress();
            }
        }
        dto.setPickPointName(pickPointName);
        dto.setPickPointAddress(pickPointAddress);
        dto.setCreateTime(order.getCreateTime() == null ? "" : order.getCreateTime().format(DATE_TIME_FORMATTER));
        return dto;
    }
}
