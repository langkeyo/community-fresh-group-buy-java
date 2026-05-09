package com.langkeyo.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TrendPointDTO {
    private String label;
    private Long orders;
    private BigDecimal gmv;
}
