package com.example.demo.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "coinbase")
@Getter
@Setter
public class CoinbaseProperties {
  private String url;
  private List<String> products;
}
