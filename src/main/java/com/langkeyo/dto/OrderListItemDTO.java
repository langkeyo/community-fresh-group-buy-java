package com.langkeyo.dto;

import lombok.Data;

@Data
public class OrderListItemDTO {
    private String id;
    private String no;
    private Long userId;
    private Long productId;
    private String name;
    private Integer qty;
    private String price;
    private String couponId;
    private String couponTitle;
    private String couponAmount;
    private String remark;
    private String payMethod;
    private String refundMethod;
    private String refundReason;
    private String refundNote;
    private String refundTime;
    private Integer status;
    private String createTime;
    private String pickPointName;
    private String pickPointAddress;
}
