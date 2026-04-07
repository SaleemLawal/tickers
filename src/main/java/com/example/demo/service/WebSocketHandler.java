package com.example.demo.service;

import com.example.demo.config.CoinbaseProperties;
import com.example.demo.kafka.KafkaProducer;
import com.example.demo.model.PriceTick;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;

import reactor.core.publisher.Mono;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

import static com.example.demo.model.PriceTick.constructPriceTick;

@Component
@Slf4j
public class WebSocketHandler implements org.springframework.web.reactive.socket.WebSocketHandler {

  private final CoinbaseProperties coinbaseProperties;
  private final KafkaProducer kafkaProducer;

  private final ObjectMapper mapper = new ObjectMapper();

  public WebSocketHandler(CoinbaseProperties coinbaseProperties, KafkaProducer kafkaProducer) {
    this.coinbaseProperties = coinbaseProperties;
    this.kafkaProducer = kafkaProducer;
  }

  @Override
  public @NotNull Mono<@NotNull Void> handle(@NotNull WebSocketSession session) {
    Map<String, Object> subscribeObj = getSubscribeObj();
    String subMessage;
    try {
      subMessage = mapper.writeValueAsString(subscribeObj);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    Mono<@NotNull WebSocketMessage> subscribe = Mono.just(session.textMessage(subMessage));

    return session
        .send(subscribe)
        .thenMany(session.receive())
        .map(WebSocketMessage::getPayloadAsText)
        .<PriceTick>handle(
            (json, sink) -> {
              try {
                Map<String, Object> payload = mapper.readValue(json, new TypeReference<>() {});
                if (payload.get("type").equals("ticker")) {
                  sink.next(constructPriceTick(payload));
                }
              } catch (JsonProcessingException e) {
                sink.error(new RuntimeException(e));
              }
            })
        .flatMap(kafkaProducer::sendMessage)
        .doOnError(e -> log.error("Error: {}", e.getMessage()))
        .then();
  }

  private @NotNull Map<String, Object> getSubscribeObj() {
    return Map.of(
        "type", "subscribe",
        "product_ids", coinbaseProperties.getProducts(),
        "channels",
            List.of(Map.of("name", "ticker", "product_ids", coinbaseProperties.getProducts())));
  }
}
