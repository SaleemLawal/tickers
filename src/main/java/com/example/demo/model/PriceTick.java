package com.example.demo.model;

import java.time.Instant;

public record PriceTick(String ticker, double price, Instant timestamp) {
}
