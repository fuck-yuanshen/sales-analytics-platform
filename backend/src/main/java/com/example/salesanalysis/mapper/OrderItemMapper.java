package com.example.salesanalysis.mapper;

import com.example.salesanalysis.domain.OrderItem;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderItemMapper {

    @Insert("""
        INSERT INTO order_items(order_id, product_id, quantity, unit_price, amount)
        VALUES(#{orderId}, #{productId}, #{quantity}, #{unitPrice}, #{amount})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OrderItem orderItem);

    @Insert("""
        INSERT INTO archived_order_items(order_id, product_id, quantity, unit_price, amount, archived_at)
        VALUES(#{orderId}, #{productId}, #{quantity}, #{unitPrice}, #{amount}, NOW())
        """)
    int archive(OrderItem orderItem);

    List<OrderItem> findByOrderIds(@Param("orderIds") List<Long> orderIds);

    int deleteByOrderIds(@Param("orderIds") List<Long> orderIds);
}
