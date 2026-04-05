package com.example.demo;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.util.retry.Retry;

import java.net.URI;
import java.time.Duration;

@Service
public class CoinbaseService {
    private final WebSocketClient client = new ReactorNettyWebSocketClient();

    private final WebSocketHandler handler;

    public CoinbaseService(WebSocketHandler handler) {
        this.handler = handler;
    }
    @PostConstruct
    public void connect() {
        String COINBASE_URL = "wss://ws-feed.exchange.coinbase.com";

        client.execute(URI.create(COINBASE_URL), handler)
                .retryWhen(Retry.backoff(10, Duration.ofSeconds(2)))
                .subscribe();
    }
}
