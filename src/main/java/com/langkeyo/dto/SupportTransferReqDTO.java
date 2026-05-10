package com.langkeyo.dto;

import lombok.Data;

@Data
public class SupportTransferReqDTO {
    private String operator;
    private String targetOperator;
    private String reason;
}
