package com.langkeyo.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderRefundReqDTO {
    private String reason;
    private BigDecimal amount;
    private String note;
    private String refundType;
}
