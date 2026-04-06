package com.langkeyo.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LeaderWorkbenchDTO {
    private Integer pendingCount;
    private Integer pickedTodayCount;
    private List<OrderListItemDTO> pendingOrders = new ArrayList<>();
    private List<OrderListItemDTO> recentPickedOrders = new ArrayList<>();
}
