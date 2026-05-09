package com.langkeyo.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class OpsOverviewDTO {
    private Long orderTotal;
    private Long orderPending;
    private Long orderGrouped;
    private Long orderPicked;
    private Long orderCancelled;

    private BigDecimal gmvTotal;
    private BigDecimal gmvPaid;

    private Long userTotal;
    private Long leaderTotal;
    private Long productTotal;
    private Long productActive;
    private Long lowStockTotal;
    private Long pickPointTotal;
    private Long supportPending;
    private Long noticeOnline;
    private Long noticeUnread;

    private List<TrendPointDTO> trendLast7Days = new ArrayList<>();
    private List<TrendPointDTO> trendLast6Months = new ArrayList<>();
}
