package com.example.demo;

import com.example.demo.model.PriceTick;
import org.jetbrains.annotations.NotNull;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

import static com.example.demo.model.PriceTick.constructPriceTick;

@Component
public class WebSocketHandler implements org.springframework.web.reactive.socket.WebSocketHandler {
    private final ObjectMapper mapper = new ObjectMapper();
    private final Sinks.Many<@NotNull PriceTick> sink = Sinks.many().multicast().directBestEffort();

    public Flux<@NotNull PriceTick> getTickerFlux() {
        return sink.asFlux();
    }

    @Override
    public @NotNull Mono<@NotNull Void> handle(WebSocketSession session) {
        String subMessage = """
            {
              "type": "subscribe",
              "product_ids": ["ETH-USD", "ETH-EUR"],
              "channels": [
                "level2",
                "heartbeat",
                {
                  "name": "ticker",
                  "product_ids": ["ETH-BTC", "ETH-USD"]
                }
              ]
            }
        """;

        Mono<@NotNull WebSocketMessage> subscribe = Mono.just(session.textMessage(subMessage));

        return session.send(subscribe)
                .thenMany(session.receive())
                .map(WebSocketMessage::getPayloadAsText)
                .flatMap(json -> {
                    try {
                        Map<String, Object> map = mapper.readValue(json, new TypeReference<>() {});
                        if (!map.get("type").equals("ticker")) {
                            return Mono.empty();
                        }
                        return Mono.just(constructPriceTick(map));
                    } catch (Exception e) {
                        return Mono.error(e);
                    }
                })
                .doOnNext(sink::tryEmitNext)
                .doOnError(e -> System.err.println("Error: " + e.getMessage()))
                .then();
    }
}