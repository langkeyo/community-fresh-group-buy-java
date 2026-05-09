package com.langkeyo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PickPointItemDTO {
    private Long id;
    private String name;
    private String address;
    private String leaderName;
    private String phone;
    private Double latitude;
    private Double longitude;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
