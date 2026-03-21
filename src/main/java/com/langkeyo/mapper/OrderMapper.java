package com.langkeyo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.langkeyo.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 订单 Mapper 接口
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
    @Update("UPDATE `orders` SET status = #{status} WHERE id = #{orderId}")
    int updateOrderStatus(@Param("orderId") String orderId, @Param("status") Integer status);
}
