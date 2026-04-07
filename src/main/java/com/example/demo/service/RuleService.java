package com.example.demo.service;

import com.example.demo.kafka.KafkaConsumer;
import com.example.demo.model.Alert;
import com.example.demo.model.PriceTick;
import com.example.demo.model.Rule;
import com.example.demo.repository.RuleRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class RuleService {
    private final RuleRepository ruleRepository;
    private final ConcurrentHashMap<String, List<Rule>> ruleMap = new ConcurrentHashMap<>();
    private final KafkaConsumer kafkaConsumer;

    public RuleService(RuleRepository ruleRepository, KafkaConsumer kafkaConsumer) {
        this.ruleRepository = ruleRepository;
        this.kafkaConsumer = kafkaConsumer;
    }

    @PostConstruct
    public void init() {
        refreshRules()
                .retryWhen(Retry.backoff(5, Duration.ofSeconds(2)))
                .then(Mono.fromRunnable(kafkaConsumer::startConsuming))
                .subscribe();
    }

    public Mono<@NotNull Void> refreshRules() {
        return ruleRepository.findRuleByEnabled(true)
                .collectList()
                .map(rules -> {
                    Map<String, List<Rule>> newMap = new HashMap<>();
                    rules.forEach(rule -> {
                        newMap.computeIfAbsent(rule.productId(), k -> new ArrayList<>())
                                .add(rule);
                    });
                    return newMap;

                })
                .doOnNext(newMap -> {
                    ruleMap.clear();
                    ruleMap.putAll(newMap);
                    log.info("Rules cache refreshed {}", ruleMap);
                })
                .then();
    }

    public Flux<@NotNull Alert> evaluate(PriceTick record) {
        // find matching rules and use that to generate alert
        return Flux.fromIterable(ruleMap.get(record.ticker()).stream().filter(r -> {
            final int compared = BigDecimal.valueOf(record.price()).compareTo(BigDecimal.valueOf(record.price()));
            switch (r.condition()) {
                case ABOVE -> {
                    return compared > 0;
                }
                case BELOW -> {
                    return compared < 0;
                }
            }
            return false;
        }).map(rule -> new Alert(1,
                rule.productId(),
                rule.condition(),
                rule.threshold(),
                BigDecimal.valueOf(record.price()),
                Instant.now())).toList());
    }
}
