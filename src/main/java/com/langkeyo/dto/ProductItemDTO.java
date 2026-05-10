package com.langkeyo.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductItemDTO {
    private Long id;
    private String name;
    private String category;
    private BigDecimal price;
    private BigDecimal groupPrice2;
    private BigDecimal groupPrice3;
    private Integer stock;
    private String images;
    private Integer status;
    private Integer groupOpen;
    private LocalDateTime groupStartTime;
    private LocalDateTime groupEndTime;
}
