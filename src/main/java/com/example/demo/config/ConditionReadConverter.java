package com.example.demo.config;

import com.example.demo.model.Condition;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class ConditionReadConverter implements Converter<@NotNull String, Condition> {
  @Override
  public Condition convert(String source) {
    return Condition.valueOf(source.toUpperCase());
  }
}
