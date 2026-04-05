package com.example.demo.service;

import com.example.demo.config.CoinbaseProperties;
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
    private final CoinbaseProperties coinbaseProperties;

    public CoinbaseService(WebSocketHandler handler, CoinbaseProperties coinbaseProperties) {
        this.handler = handler;
        this.coinbaseProperties = coinbaseProperties;
    }
    @PostConstruct
    public void connect() {

        client.execute(URI.create(coinbaseProperties.getUrl()), handler)
                .retryWhen(Retry.backoff(10, Duration.ofSeconds(2)))
                .subscribe();
    }
}
