package com.example.demo.service;

import com.example.demo.kafka.KafkaConsumer;
import com.example.demo.model.Alert;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
public class AppStartupService {
  private final RuleService ruleService;
  private final KafkaConsumer kafkaConsumer;

  public AppStartupService(RuleService ruleService, KafkaConsumer kafkaConsumer) {
    this.ruleService = ruleService;
    this.kafkaConsumer = kafkaConsumer;
  }

  @PostConstruct
  public void start() {
    ruleService
        .refreshRules()
        .retryWhen(Retry.backoff(5, Duration.ofSeconds(2)))
        .<Alert>then(Mono.fromRunnable(kafkaConsumer::startConsuming))
        .subscribe();
  }
}
