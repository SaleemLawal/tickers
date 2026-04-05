package com.example.demo;

import com.example.demo.model.PriceTick;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Random;

@RestController
public class PriceController {

    @GetMapping(value = "/ticks", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<@NotNull PriceTick> demo() {
        final Random rand = new Random();

        List<String> tickers = List.of("AAPL", "AMZN", "NTFL", "GOGL", "NASA", "UBER", "NVDI");
        return Flux.interval(Duration.ofSeconds(1))
                   .map(_ -> new PriceTick(tickers.get(rand.nextInt(tickers.size())),
                           rand.nextDouble(5000),
                           Instant.now()));
    }


}
