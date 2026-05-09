package com.langkeyo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.langkeyo.common.Result;
import com.langkeyo.dto.OpsOverviewDTO;
import com.langkeyo.dto.TrendPointDTO;
import com.langkeyo.entity.*;
import com.langkeyo.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/ops")
public class OpsController {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private PickPointMapper pickPointMapper;
    @Autowired
    private SupportTicketMapper supportTicketMapper;
    @Autowired
    private NoticeMapper noticeMapper;
    @Autowired
    private NoticeReadMapper noticeReadMapper;

    @GetMapping("/overview")
    public Result<OpsOverviewDTO> overview(@RequestParam(defaultValue = "1") Long userId) {
        OpsOverviewDTO dto = new OpsOverviewDTO();

        List<Order> orders = orderMapper.selectList(new QueryWrapper<>());
        dto.setOrderTotal((long) orders.size());
        dto.setOrderPending(orders.stream().filter(o -> Objects.equals(o.getStatus(), 1)).count());
        dto.setOrderGrouped(orders.stream().filter(o -> Objects.equals(o.getStatus(), 2)).count());
        dto.setOrderPicked(orders.stream().filter(o -> Objects.equals(o.getStatus(), 3)).count());
        dto.setOrderCancelled(orders.stream().filter(o -> Objects.equals(o.getStatus(), -1)).count());

        BigDecimal gmvTotal = orders.stream()
                .map(Order::getTotalPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal gmvPaid = orders.stream()
                .filter(o -> Objects.equals(o.getStatus(), 2) || Objects.equals(o.getStatus(), 3))
                .map(Order::getTotalPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setGmvTotal(gmvTotal);
        dto.setGmvPaid(gmvPaid);

        List<User> users = userMapper.selectList(new QueryWrapper<>());
        dto.setUserTotal((long) users.size());
        dto.setLeaderTotal(users.stream().filter(u -> Boolean.TRUE.equals(u.getIsLeader())).count());

        List<Product> products = productMapper.selectList(new QueryWrapper<>());
        dto.setProductTotal((long) products.size());
        dto.setProductActive(products.stream().filter(p -> Objects.equals(p.getStatus(), 1)).count());
        dto.setLowStockTotal(products.stream()
                .filter(p -> p.getStock() != null && p.getStock() <= 20)
                .count());

        dto.setPickPointTotal((long) pickPointMapper.selectList(new QueryWrapper<>()).size());
        dto.setSupportPending((long) supportTicketMapper.selectList(new QueryWrapper<SupportTicket>()
                .eq("status", 1)).size());

        List<Notice> onlineNotices = noticeMapper.selectList(new QueryWrapper<Notice>().eq("status", 1));
        dto.setNoticeOnline((long) onlineNotices.size());
        if (onlineNotices.isEmpty()) {
            dto.setNoticeUnread(0L);
        } else {
            List<Long> noticeIds = onlineNotices.stream().map(Notice::getId).collect(Collectors.toList());
            Long readCount = (long) noticeReadMapper.selectList(new QueryWrapper<NoticeRead>()
                    .eq("user_id", userId)
                    .in("notice_id", noticeIds)).size();
            dto.setNoticeUnread(Math.max(0L, (long) onlineNotices.size() - readCount));
        }

        dto.setTrendLast7Days(buildLast7Days(orders));
        dto.setTrendLast6Months(buildLast6Months(orders));
        return Result.success(dto);
    }

    private List<TrendPointDTO> buildLast7Days(List<Order> orders) {
        Map<LocalDate, TrendPointDTO> map = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            TrendPointDTO point = new TrendPointDTO();
            point.setLabel(day.toString());
            point.setOrders(0L);
            point.setGmv(BigDecimal.ZERO);
            map.put(day, point);
        }
        for (Order order : orders) {
            LocalDateTime createTime = order.getCreateTime();
            if (createTime == null) continue;
            TrendPointDTO point = map.get(createTime.toLocalDate());
            if (point == null) continue;
            point.setOrders(point.getOrders() + 1);
            point.setGmv(point.getGmv().add(order.getTotalPrice() == null ? BigDecimal.ZERO : order.getTotalPrice()));
        }
        return new ArrayList<>(map.values());
    }

    private List<TrendPointDTO> buildLast6Months(List<Order> orders) {
        Map<YearMonth, TrendPointDTO> map = new LinkedHashMap<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth month = YearMonth.now().minusMonths(i);
            TrendPointDTO point = new TrendPointDTO();
            point.setLabel(month.toString());
            point.setOrders(0L);
            point.setGmv(BigDecimal.ZERO);
            map.put(month, point);
        }
        for (Order order : orders) {
            LocalDateTime createTime = order.getCreateTime();
            if (createTime == null) continue;
            TrendPointDTO point = map.get(YearMonth.from(createTime));
            if (point == null) continue;
            point.setOrders(point.getOrders() + 1);
            point.setGmv(point.getGmv().add(order.getTotalPrice() == null ? BigDecimal.ZERO : order.getTotalPrice()));
        }
        return new ArrayList<>(map.values());
    }
}
