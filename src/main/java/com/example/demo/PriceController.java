package com.example.demo;

import com.example.demo.model.PriceTick;
import com.example.demo.service.WebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
public class PriceController {

    private final WebSocketHandler handler;

    @GetMapping(value = "/ticks", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<@NotNull PriceTick> demo() {
        return handler.getTickerFlux();
    }
}
