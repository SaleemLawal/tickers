package com.example.demo.model;

import java.math.BigDecimal;
import java.time.Instant;

public record Alert(
    Integer ruleId,
    String productId,
    Condition condition,
    BigDecimal threshold,
    BigDecimal actualPrice,
    Instant triggeredAt) {}
