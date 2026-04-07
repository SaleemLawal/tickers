package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.PostgresDialect;

import java.util.List;

@Configuration
public class R2dbcConfig {

  @Bean
  R2dbcCustomConversions r2dbcCustomConversions() {
    return R2dbcCustomConversions.of(
        PostgresDialect.INSTANCE, List.of(new ConditionReadConverter()));
  }
}
