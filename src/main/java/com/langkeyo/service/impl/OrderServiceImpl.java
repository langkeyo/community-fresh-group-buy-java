package com.langkeyo.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.langkeyo.dto.LeaderWorkbenchDTO;
import com.langkeyo.dto.OpenGroupItemDTO;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private PickPointMapper pickPointMapper;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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

        if (order.getGroupBuyId() == null || order.getGroupBuyId().trim().isEmpty()) {
            order.setGroupBuyId("GB2-" + IdUtil.fastSimpleUUID());
        }

        if (order.getProductId() != null) {
            int updated = productMapper.decreaseStock(order.getProductId());
            if (updated == 0) {
                return false;
            }
        }

        boolean saved = this.save(order);
        if (!saved) {
            return false;
        }
        tryAutoCompleteGroup(order.getGroupBuyId(), order.getProductId(), order.getPickPointId());
        return true;
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
    public List<OrderListItemDTO> getAllOrders(Integer status, Long pickPointId, String startTime, String endTime) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            queryWrapper.eq(Order::getStatus, status);
        }
        if (pickPointId != null) {
            queryWrapper.eq(Order::getPickPointId, pickPointId);
        }

        LocalDateTime start = parseStartTime(startTime);
        LocalDateTime end = parseEndTime(endTime);
        if (start != null) {
            queryWrapper.ge(Order::getCreateTime, start);
        }
        if (end != null) {
            queryWrapper.le(Order::getCreateTime, end);
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
    public LeaderWorkbenchDTO getLeaderWorkbench(Long pickPointId) {
        LeaderWorkbenchDTO dto = new LeaderWorkbenchDTO();
        if (pickPointId == null) {
            dto.setPendingCount(0);
            dto.setPickedTodayCount(0);
            return dto;
        }

        LambdaQueryWrapper<Order> pendingQw = new LambdaQueryWrapper<>();
        pendingQw.eq(Order::getPickPointId, pickPointId)
                .eq(Order::getStatus, 2)
                .orderByDesc(Order::getCreateTime);
        List<Order> pendingOrders = this.list(pendingQw);

        LambdaQueryWrapper<Order> recentPickedQw = new LambdaQueryWrapper<>();
        recentPickedQw.eq(Order::getPickPointId, pickPointId)
                .eq(Order::getStatus, 3)
                .orderByDesc(Order::getUpdateTime)
                .last("limit 5");
        List<Order> recentPickedOrders = this.list(recentPickedQw);

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        LambdaQueryWrapper<Order> todayPickedQw = new LambdaQueryWrapper<>();
        todayPickedQw.eq(Order::getPickPointId, pickPointId)
                .eq(Order::getStatus, 3)
                .ge(Order::getUpdateTime, todayStart)
                .le(Order::getUpdateTime, todayEnd);
        int pickedTodayCount = Math.toIntExact(this.count(todayPickedQw));

        Set<Long> productIds = new HashSet<>();
        for (Order item : pendingOrders) {
            if (item.getProductId() != null) {
                productIds.add(item.getProductId());
            }
        }
        for (Order item : recentPickedOrders) {
            if (item.getProductId() != null) {
                productIds.add(item.getProductId());
            }
        }

        Map<Long, String> productNameMap = new HashMap<>();
        if (!productIds.isEmpty()) {
            List<Product> products = productMapper.selectNameListByIds(List.copyOf(productIds));
            for (Product product : products) {
                productNameMap.put(product.getId(), product.getName());
            }
        }

        List<OrderListItemDTO> pendingDtos = pendingOrders.stream()
                .map(order -> toOrderListItemDTO(order, productNameMap))
                .collect(Collectors.toList());
        List<OrderListItemDTO> recentDtos = recentPickedOrders.stream()
                .map(order -> toOrderListItemDTO(order, productNameMap))
                .collect(Collectors.toList());

        dto.setPendingCount(pendingDtos.size());
        dto.setPickedTodayCount(pickedTodayCount);
        dto.setPendingOrders(pendingDtos);
        dto.setRecentPickedOrders(recentDtos);
        return dto;
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

    @Override
    public List<OpenGroupItemDTO> listOpenGroups(Long productId, Long pickPointId) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(productId != null, Order::getProductId, productId);
        queryWrapper.eq(pickPointId != null, Order::getPickPointId, pickPointId);
        queryWrapper.eq(Order::getStatus, 1);
        queryWrapper.isNotNull(Order::getGroupBuyId);
        queryWrapper.orderByDesc(Order::getCreateTime);

        List<Order> orders = this.list(queryWrapper);
        Map<String, List<Order>> grouped = orders.stream()
                .filter(o -> o.getGroupBuyId() != null && !o.getGroupBuyId().trim().isEmpty())
                .collect(Collectors.groupingBy(Order::getGroupBuyId, LinkedHashMap::new, Collectors.toList()));

        List<OpenGroupItemDTO> result = new ArrayList<>();
        for (Map.Entry<String, List<Order>> entry : grouped.entrySet()) {
            String groupId = entry.getKey();
            List<Order> list = entry.getValue();
            int current = list.size();
            int target = parseTargetCount(groupId);
            if (current >= target) {
                continue;
            }

            OpenGroupItemDTO dto = new OpenGroupItemDTO();
            dto.setGroupBuyId(groupId);
            dto.setCurrentCount(current);
            dto.setTargetCount(target);
            LocalDateTime latest = list.stream()
                    .map(Order::getCreateTime)
                    .filter(Objects::nonNull)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);
            dto.setLatestCreateTime(latest == null ? "" : latest.format(DATE_TIME_FORMATTER));
            result.add(dto);
        }
        return result;
    }

    @Override
    public Optional<OpenGroupItemDTO> getOpenGroup(String groupBuyId, Long productId, Long pickPointId) {
        if (groupBuyId == null || groupBuyId.trim().isEmpty()) {
            return Optional.empty();
        }
        List<OpenGroupItemDTO> list = listOpenGroups(productId, pickPointId);
        return list.stream()
                .filter(item -> groupBuyId.equals(item.getGroupBuyId()))
                .findFirst();
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

    private int parseTargetCount(String groupBuyId) {
        if (groupBuyId == null) return 2;
        if (groupBuyId.startsWith("GB3-")) return 3;
        if (groupBuyId.startsWith("GB2-")) return 2;
        return 2;
    }

    private void tryAutoCompleteGroup(String groupBuyId, Long productId, Long pickPointId) {
        if (groupBuyId == null || groupBuyId.trim().isEmpty()) {
            return;
        }
        int target = parseTargetCount(groupBuyId);

        LambdaQueryWrapper<Order> qw = new LambdaQueryWrapper<>();
        qw.eq(Order::getGroupBuyId, groupBuyId);
        qw.eq(productId != null, Order::getProductId, productId);
        qw.eq(pickPointId != null, Order::getPickPointId, pickPointId);
        qw.eq(Order::getStatus, 1);
        List<Order> grouped = this.list(qw);
        if (grouped.size() < target) {
            return;
        }

        for (Order row : grouped) {
            baseMapper.updateOrderStatus(row.getId(), 2);
        }
    }

    private LocalDateTime parseStartTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String text = value.trim();
        try {
            if (text.length() == 10) {
                return LocalDate.parse(text, DATE_FORMATTER).atStartOfDay();
            }
            return LocalDateTime.parse(text, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private LocalDateTime parseEndTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String text = value.trim();
        try {
            if (text.length() == 10) {
                return LocalDate.parse(text, DATE_FORMATTER).atTime(LocalTime.MAX);
            }
            return LocalDateTime.parse(text, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }
}
