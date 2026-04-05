package com.langkeyo.dto;

import lombok.Data;

import java.math.BigDecimal;

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
}
