package com.example.demo.repository;

import com.example.demo.model.Rule;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface RuleRepository extends R2dbcRepository<@NotNull Rule, @NotNull Long> {
  Flux<@NotNull Rule> findRuleByEnabled(boolean enabled);
}
