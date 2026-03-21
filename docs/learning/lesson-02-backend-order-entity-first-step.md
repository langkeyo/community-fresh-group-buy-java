# Lesson 02：订单模块第一步，先建 `Order` 实体

## 这节课学什么

这节课不急着写 Controller，也不急着写接口。

先做后端新业务模块最关键的第一步：

**把订单的数据地基先立起来。**

也就是先建：

`Order` 实体类

---

## 为什么后端第一步先建实体类

因为订单模块后面所有层，都会依赖它：

- `Controller` 要知道返回什么
- `Service` 要知道处理什么
- `Mapper` 要知道映射什么
- 数据库表也要知道存什么

如果订单字段都没想清楚，后面所有层都会漂。

所以这一步不是“先写个类玩玩”，而是在做：

**订单模块的数据地基。**

---

## 这次先做“最小可用订单模型”

先不要追求大而全。

结合你前端现在已经做好的页面，订单最小字段先定成这些：

1. `id`
2. `orderNo`
3. `userId`
4. `productName`
5. `groupType`
6. `amount`
7. `status`
8. `pickupPoint`
9. `receiverName`
10. `mobile`
11. `createTime`
12. `updateTime`
13. `deleted`

---

## 每个字段为什么存在

### `id`

数据库主键，后端内部识别订单。

### `orderNo`

订单号，给前端展示。

你前端订单页已经有“订单号”这个展示位，所以后端必须有对应字段。

### `userId`

表示这笔订单属于谁。

以后登录态、查我的订单、订单列表过滤，都会依赖它。

### `productName`

当前毕设阶段先用单商品简化模型，所以先直接存商品名。

以后如果你要升级成标准电商结构，再拆成：

- 订单主表
- 订单项表

现在先别把自己绕进去。

### `groupType`

2 人团还是 3 人团。

这和你前端 `formData.groupType` 正好对应。

### `amount`

订单金额。

### `status`

订单状态。

当前你前端已经有：

- 待成团
- 已成团
- 已取货

所以后端也要先有这个字段。

### `pickupPoint`

自提点。

### `receiverName`

收件人姓名。

### `mobile`

手机号。

注意：这里应该继续用 `String`，不要用数字类型。

### `createTime / updateTime / deleted`

这三个字段是你当前项目实体层已经在用的统一风格，订单模块应该延续。

---

## 这一步你要建哪个文件

文件路径：

`src/main/java/com/langkeyo/entity/Order.java`

---

## 你先照着这个结构写

```java
package com.langkeyo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Order implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("order_no")
    private String orderNo;

    @TableField("user_id")
    private Long userId;

    @TableField("product_name")
    private String productName;

    @TableField("group_type")
    private Integer groupType;

    @TableField("amount")
    private BigDecimal amount;

    @TableField("status")
    private String status;

    @TableField("pickup_point")
    private String pickupPoint;

    @TableField("receiver_name")
    private String receiverName;

    @TableField("mobile")
    private String mobile;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
```

---

## 为什么 `amount` 要用 `BigDecimal`

因为金额字段不能用 `double`。

原因很简单：

- `double` 有精度问题
- 金额不能容忍精度漂移
- Java 后端里金额字段通常优先用 `BigDecimal`

你可以把它和手机号字段一起记：

- 金额：按“钱”的业务意义选 `BigDecimal`
- 手机号：按“标识符”的业务意义选 `String`

不是看起来像什么就用什么。

---

## 这一步先不要做什么

先不要急着：

- 建 `OrderController`
- 建 `OrderMapper`
- 写创建订单接口
- 配 RabbitMQ

这节课只做一件事：

**先把订单实体写准。**

---

## 你的本轮任务

1. 新建 `Order.java`
2. 按这份结构敲进去
3. 自己说出下面这句话：

`为什么订单模块第一步要先从实体类开始，而不是先写接口？`

---

## 下一课预告

下一课我们会继续做订单模块的第二步，大概率是这两个里的一个：

1. 建 `OrderMapper`
2. 建 `IOrderService`

到那时你会开始真正感受到后端“新业务模块是怎么一层层长出来的”。
