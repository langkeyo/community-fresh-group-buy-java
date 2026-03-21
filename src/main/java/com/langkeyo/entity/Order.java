package com.langkeyo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Order  implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId("id")
    private String id;

    @TableField("user_id")
    private Long userId;

    @TableField("product_id")
    private Long productId;

    @TableField("total_price")
    private BigDecimal totalPrice;

    @TableField("status")
    private Integer status;

    @TableField("pick_point_id")
    private Long pickPointId;

    @TableField("group_buy_id")
    private String groupBuyId;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
