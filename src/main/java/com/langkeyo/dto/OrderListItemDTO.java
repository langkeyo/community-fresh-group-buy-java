package com.langkeyo.dto;

import lombok.Data;

@Data
public class OrderListItemDTO {
    private String id;
    private String no;
    private Long productId;
    private String name;
    private Integer qty;
    private String price;
    private Integer status;
    private String createTime;
    private String pickPointName;
    private String pickPointAddress;
}
