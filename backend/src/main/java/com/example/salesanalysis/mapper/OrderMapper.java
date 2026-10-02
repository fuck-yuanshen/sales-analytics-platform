package com.example.salesanalysis.mapper;

import com.example.salesanalysis.domain.OrderRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {

    @Insert("""
        INSERT INTO orders(order_no, user_id, order_type, payment_status, placed_at, paid_at, total_amount, total_quantity, created_at, updated_at)
        VALUES(#{orderNo}, #{userId}, #{orderType}, #{paymentStatus}, #{placedAt}, #{paidAt}, #{totalAmount}, #{totalQuantity}, NOW(), NOW())
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OrderRecord orderRecord);

    @Select("SELECT * FROM orders WHERE placed_at < #{archiveBefore}")
    List<OrderRecord> findArchivable(@Param("archiveBefore") LocalDateTime archiveBefore);

    @Insert("""
        INSERT INTO archived_orders(order_no, user_id, order_type, payment_status, placed_at, paid_at, total_amount, total_quantity, archived_at)
        VALUES(#{orderNo}, #{userId}, #{orderType}, #{paymentStatus}, #{placedAt}, #{paidAt}, #{totalAmount}, #{totalQuantity}, NOW())
        """)
    int archive(OrderRecord orderRecord);

    int deleteByIds(@Param("ids") List<Long> ids);

    List<OrderRecord> findByOrderNos(@Param("orderNos") List<String> orderNos);
}
