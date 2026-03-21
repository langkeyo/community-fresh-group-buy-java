package com.langkeyo.dto;

import lombok.Data;

@Data
public class OrderListItemDTO {
    private String id;
    private String no;
    private String name;
    private Integer qty;
    private String price;
    private Integer status;
    private String createTime;
}
