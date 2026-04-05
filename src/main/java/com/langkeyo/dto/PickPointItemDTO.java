package com.langkeyo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PickPointItemDTO {
    private Long id;
    private String name;
    private String address;
}
