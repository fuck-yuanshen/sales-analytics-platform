package com.example.salesanalysis.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderRecord {
    private Long id;
    private String orderNo;
    private Long userId;
    private String orderType;
    private String paymentStatus;
    private LocalDateTime placedAt;
    private LocalDateTime paidAt;
    private BigDecimal totalAmount;
    private Integer totalQuantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
