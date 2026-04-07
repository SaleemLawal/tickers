package com.example.demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Table("rules")
public record Rule(
        @Id
        Integer id,

        @Column("product_id")
        String productId,
        Condition condition,
        BigDecimal threshold,
        boolean enabled,
        
        @Column("created_at")
        Instant createdAt
) {
}
