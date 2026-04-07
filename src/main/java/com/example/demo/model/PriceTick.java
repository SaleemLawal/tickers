package com.example.demo.model;

import java.time.Instant;
import java.util.Map;

public record PriceTick(String ticker, double price, Instant timestamp) {

  public static PriceTick constructPriceTick(Map<String, Object> map) {
    Object priceObj = map.getOrDefault("price", "0.0");
    double price = Double.parseDouble(priceObj.toString());

    String productId = map.getOrDefault("product_id", "UNKNOWN").toString();

    Object timeObj = map.get("time");
    Instant time = (timeObj != null) ? Instant.parse(timeObj.toString()) : Instant.now();

    return new PriceTick(productId, price, time);
  }
}
